package StockMicroservice;

public class StockRabbitMQConfig {
    @Bean
    public Exchange quotaEventExchange() {
        return new TopicExchange("quota-event-exchange", true, false);
    }

    /**
     * Delay queue, trigger when "quota.lock" becomes dead letter
     * @return
     */
    @Bean
    public Queue quotaDelayQueue() {
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "quota-event-exchange");
        arguments.put("x-dead-letter-routing-key", "quota.release");
        // delay 50 mins, milliseconds
        arguments.put("x-message-ttl", 3000000);
        return new Queue("quota.delay.queue", true, false, false, arguments);
    }

    /**
     * Normal message queue, used to unlock lesson quota
     * @return
     */
    @Bean
    public Queue quotaReleasequotaQueue() {
        return new Queue("quota.release.quota.queue", true, false, false, null);
    }


    /**
     * Exchange and delay queue binding
     * @return
     */
    @Bean
    public Binding quotaLockedBinding() {
        return new Binding("quota.delay.queue",
                Binding.DestinationType.QUEUE,
                "quota-event-exchange",
                "quota.locked",
                null);
    }

    /**
     * Exchange and normal queue binding
     * @return
     */
    @Bean
    public Binding quotaReleaseBinding() {
        return new Binding("quota.release.quota.queue",
                Binding.DestinationType.QUEUE,
                "quota-event-exchange",
                "quota.release.#",
                null);
    }
}
