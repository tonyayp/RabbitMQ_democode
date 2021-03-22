package StockMicroservice;

public class StockServiceImpl {

    @Override
    public void unlock(StockLockedTo stockLockedTo) {
        StockDetailTo detailTo = stockLockedTo.getDetailTo();
        WareOrderTaskDetailEntity detailEntity = wareOrderTaskDetailService.getById(detailTo.getId());

        //1. if detail entity is not null, that means stock was locked successfully in previous steps, now need to unlock stock
        if (detailEntity != null) {
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(stockLockedTo.getId());
            R r = orderFeignService.infoByOrderSn(taskEntity.getOrderSn());
            if (r.getCode() == 0) {
                OrderTo order = r.getData("order", new TypeReference<OrderTo>() {
                });

                // there is no such order || order already cancelled and release stock now
                if (order == null||order.getStatus()== OrderStatusEnum.CANCLED.getCode()) {

                    // to ensure idempotency, detail entity lock status is checked once again to make sure it is in lock status at this moment.
                    // to prevent situations where, possibly late payment or delayed network status
                    if (detailEntity.getLockStatus()== WareTaskStatusEnum.Locked.getCode()){
                        unlockStock(detailTo.getSkuId(), detailTo.getSkuNum(), detailTo.getWareId(), detailEntity.getId());
                    }
                }
            }else {
                throw new RuntimeException("Feign service call for order service has ERROR");
            }
        }else {
            //doesn't require unlock, no action needed
        }
    }
}
