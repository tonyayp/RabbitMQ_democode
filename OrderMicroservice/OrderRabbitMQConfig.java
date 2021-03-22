package OrderMicroservice;

public class OrderRabbitMQConfig {
    @Bean
    public Exchange orderEventExchange() {
        /**
         *   String name,
         *   boolean durable,
         *   boolean autoDelete,
         *   Map<String, Object> arguments
         */
        return new TopicExchange("order-event-exchange", true, false);
    }

    /**
     * Delay queue
     * @return
     */
    @Bean
    public Queue orderDelayQueue() {
        /**
         Queue(String name,
         boolean durable,
         boolean exclusive,
         boolean autoDelete,
         Map<String, Object> arguments)
         */
        HashMap<String, Object> arguments = new HashMap<>();
        //Dead letter exchange
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        //Dead letter routing
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        arguments.put("x-message-ttl", 1800000); // ms
        return new Queue("order.delay.queue",true,false,false,arguments);
    }

    /**
     * Normal message queue
     *
     * @return
     */
    @Bean
    public Queue orderReleaseQueue() {

        Queue queue = new Queue("order.release.order.queue", true, false, false);

        return queue;
    }

    /**
     * binding
     * @return
     */
    @Bean
    public Binding orderCreateBinding() {
        /**
         * String destination,
         * DestinationType destinationType, （Queue、Exhcange）
         * String exchange,
         * String routingKey,
         * Map<String, Object> arguments
         * */
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.create.order", null);
    }

    @Bean
    public Binding orderReleaseBinding() {
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);
    }

    @Bean
    public Binding orderReleaseOtherBinding() {
        return new Binding("quota.release.quota.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",
                null);
    }
}
