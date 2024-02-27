package com.atguigu.gmall.list.listener;

import com.atguigu.gmall.list.service.GoodsService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听商品上下架同步消息  用于监听RabbitMQ中的商品上下架消息
 */
@Component
@Log4j2
public class SkuUpperOrDownListener {

    @Autowired
    private GoodsService goodsService;

    /**
     * 监听上架消息
     * @RabbitListener注解来指定它应该监听哪些队列
     * 这个注解表明skuUpper方法应该监听名为sku_upper_queue的RabbitMQ队列。当队列中有新消息时，这个方法将被调用。
     * @param message       RabbitMQ消息对象
     * @param channel       RabbitMQ通道对象
     */
    @RabbitListener(queues = "sku_upper_queue")
    public void skuUpper(Message message, Channel channel) {
        /** 获取消息的属性
         * MessageProperties是RabbitMQ提供的一个类，它包含了消息的属性信息，如消息ID、时间戳、延迟、优先级、交换机名称、路由键等。
         */
        MessageProperties messageProperties = message.getMessageProperties();
        /** 获取消息的编号
         * 这行代码是在处理RabbitMQ消息时获取消息的唯一标识符（delivery tag）。
         * 在RabbitMQ中，每个消息在被发送到队列时都会被分配一个唯一的序列号，这个序列号就是delivery tag。
         * 这个标识符对于确认消息（acknowledging messages）和拒绝消息（rejecting messages）非常重要。
         * 在RabbitMQ中，当你从队列中接收到一个消息并处理它时，你可以使用delivery tag来告诉RabbitMQ你已经处理完这个消息。
         * 这通常通过发送一个确认（ack）来完成。例如，你可以调用channel.basicAck(deliveryTag, false)来确认消息，
         * 这会告诉RabbitMQ可以安全地删除这个消息。如果你在处理消息时遇到错误，你可以使用delivery tag来拒绝这个消息，
         * 这样消息会被放回队列（如果设置了重新投递）或者直接丢弃（如果没有设置重新投递）。
         */
        Long deliveryTag = messageProperties.getDeliveryTag();
        /** 获取消息的内容
         * 在RabbitMQ中，message.getBody() 方法用于获取消息的内容，即消息体（message body）。
         * 消息体是消息中实际携带的数据，它可以是任何形式的字节数据，比如字符串、序列化的对象等。
         * getBody() 是 Message 类的一个方法，它返回消息的字节数据。这个方法通常返回一个 byte[] 数组，包含了消息体的原始字节数据。
         */
        Long skuId = Long.parseLong(new String(message.getBody()));
        try {
            // 完成上架
            goodsService.dbSkuAddIntoEs(skuId);
            /**
             * 确认消息，表示消息已被成功处理。
             * 这行代码是在RabbitMQ消息处理中用于确认（acknowledge）消息的操作。在RabbitMQ中，当消费者接收到消息并成功处理后，
             * 它需要向RabbitMQ服务器发送一个确认信号，告知服务器该消息已经被正确处理。这个确认操作是通过Channel对象的basicAck方法来完成的。
             * channel 是一个 Channel 对象，它代表了与RabbitMQ服务器的一个通信通道。通过这个通道，消费者可以发送和接收消息，以及执行其他与消息相关的操作。
             * basicAck 是 Channel 类的一个方法，用于发送一个基本的确认信号。这个方法告诉RabbitMQ服务器，指定的消息已经被消费者成功处理，可以将其从队列中移除。
             * 这是 basicAck 方法的第二个参数，它是一个布尔值。当这个值为 false 时，表示只确认当前处理的单个消息。
             * 如果设置为 true，则表示确认所有小于或等于指定 deliveryTag 的消息。在这个例子中，我们只确认当前处理的单个消息。
             * 当你调用 channel.basicAck(deliveryTag, false) 时，你告诉RabbitMQ服务器，与 deliveryTag 对应的消息已经被成功处理，
             * RabbitMQ可以将这个消息从队列中删除。这是一种确保消息不会丢失或重复处理的机制。
             * 如果在处理消息时发生异常，你可以选择不发送确认信号，这样消息将被重新投递给其他消费者，
             * 或者你可以发送一个拒绝信号（channel.basicNack 或 channel.basicReject），并根据需要重新投递或丢弃消息。
             */
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            try {
                // 如果操作失败，根据是否是重试消息（getRedelivered()），决定是拒绝消息并放回队列（basicReject）还是从队列中移除消息（basicReject with false）。
                /**
                 * 这行代码用于检查接收到的RabbitMQ消息是否是重新投递（redelivered）的。
                 * 在RabbitMQ中，如果消息在被消费后没有得到确认（acknowledged），或者在处理过程中出现了异常，它可能会被重新投递给其他消费者。
                 * getRedelivered() 是 MessageProperties 类的一个方法，它返回一个布尔值。
                 * 如果消息是重新投递的，即它之前已经被消费过一次但没有得到确认，那么这个方法返回 true。如果消息是首次投递，那么返回 false。
                 * 在消息处理逻辑中，你可以使用 getRedelivered() 方法来判断消息是否是新的还是之前处理失败后重新投递的。这有助于你决定如何处理这个消息。
                 */
                if (messageProperties.getRedelivered()) {   // 消息消费失败出现异常,判断消息是否第一次被消费
                    e.printStackTrace();
                    // 第二次, 记录日志,从队列移除
                    log.error("商品上架失败,商品的id为: " + skuId);
                    /**
                     * 这行代码是RabbitMQ消息处理中的一个操作，用于拒绝（reject）接收到的消息。
                     * 当消费者无法处理某个消息，或者在处理过程中遇到错误，但又不希望立即从队列中删除该消息时，
                     * 可以使用basicReject方法将消息退回到队列中，以便其他消费者可以重新尝试处理它。
                     * basicReject 是 Channel 类的一个方法，用于拒绝（reject）一个特定的消息。
                     * 这个方法会将消息标记为未被当前消费者处理，并将其放回队列中。
                     * 当这个值为 false 时，表示只拒绝当前处理的单个消息。如果设置为 true，则表示拒绝所有小于或等于指定 deliveryTag 的消息。
                     * 在这个例子中，我们只拒绝当前处理的单个消息。
                     */
                    channel.basicReject(deliveryTag, false);
                } else {    // 第一次: 拒绝消费,放回队列
                    channel.basicReject(deliveryTag, true);
                }
            } catch (Exception e1) {
                log.error("商品上架拒绝消息失败, 失败的商品id为:" + skuId + ",失败的原因为:" + e1.getMessage());
            }
        }
    }

    /**
     * 监听商品下架消息
     * 这个注解表明skuDown方法应该监听名为sku_down_queue的RabbitMQ队列。当队列中有新消息时，这个方法将被调用。
     * @param message
     * @param channel
     */
    @RabbitListener(queues = "sku_down_queue")
    public void skuDown(Message message, Channel channel) {
        // 获取消息的属性
        MessageProperties messageProperties = message.getMessageProperties();
        // 获取消息的编号
        Long deliveryTag = messageProperties.getDeliveryTag();
        // 获取消息的内容
        Long skuId = Long.parseLong(new String(message.getBody()));
        try {
            // 完成下架
            goodsService.removeGoodsFromEs(skuId);
            // 确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            try {
                // 消息消费失败出现异常,判断消息是否第一次被消费
                if (messageProperties.getRedelivered()) {
                    // 第二次, 记录日志,从队列移除
                    log.error("商品下架失败,商品的id为: " + skuId);
                    channel.basicReject(deliveryTag, false);
                } else {
                    // 第一次: 拒绝消费,放回队列
                    channel.basicReject(deliveryTag, true);
                }
            } catch (Exception e1) {
                log.error("商品下架拒绝消息失败, 失败的商品id为:" + skuId + ",失败的原因为:" + e1.getMessage());
            }
        }
    }
}