package OrderMicroservice;

public class OrderServiceImpl {

    /**
     * when student place order, will call for method to lock stock and generate rabbitmq delay queue
     * @param stockSkuLockVo
     * @return true: successfully locked order and can continue to make payment
     */
    @Transactional
    @Override
    public Boolean orderLockStock(StockSkuLockVo stockSkuLockVo) {
        // since it's possible order might roll back, but stock feign service might not have rollen back in a full transaction.
        // but if order has rollen back, stock lock details might lose forever, therefore we need a new record to correctly process remaining stocks.

        // call method to lock stock
        Long count = baseMapper.lockStockSku(stockSkuLockVo.getSkuId(), stockSkuLockVo.getNum());

        if (count == 0) {
            lock = false;
        } else {
            //lock successful, save details and record lock status
            StockOrderTaskDetailEntity detailEntity = StockOrderTaskDetailEntity.builder()
                    .skuId(stockSkuLockVo.getSkuId())
                    .skuName("")
                    .taskId(new UUID())
                    .stockId(StockSkuLockVo.getStockId())
                    .lockStatus(1).build();
            stockOrderTaskDetailService.save(detailEntity);

            // send message delay queue to monitor stock lock status
            StockLockedTo lockedTo = new StockLockedTo();
            lockedTo.setId(detailEntity.getTaskId());
            lockedTo.setDetailTo(detailEntity);
            rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);

            lock = true;
            break;
        }

        if (!lock) throw new NoStockException(skuId);

        return true;
    }
}
