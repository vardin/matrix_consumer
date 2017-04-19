package matrix_consumer;

import jcuda_matrix.jcuda_matrix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;



public class matrix_consumer {
		
	private static final String TOPIC = "supercom";
	private static final int NUM_THREADS = 10;
	
//	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

	public static void main(String[] args) throws Exception {
		
	//	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		jcuda_matrix jcuda = new jcuda_matrix(529);
		Properties props = new Properties();
		props.put("group.id", "super-group");
		props.put("zookeeper.connect", "163.152.174.73:2182");
		props.put("auto.commit.interval.ms", "100");
		ConsumerConfig consumerConfig = new ConsumerConfig(props);
		ConsumerConnector consumer = Consumer.createJavaConsumerConnector(consumerConfig);
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(TOPIC, NUM_THREADS);
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
		List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(TOPIC);
		ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
		
		
		for (final KafkaStream<byte[], byte[]> stream : streams) {

			executor.execute(new Runnable() {

				public void run() {

		//			MyFrame frame = new MyFrame();

					for (MessageAndMetadata<byte[], byte[]> messageAndMetadata : stream) {

						byte[] test = messageAndMetadata.message();

						
						
	//					Mat data = new Mat(480, 640, CvType.CV_8UC3);

	//					data.put(0, 0, test);

	//					frame.setVisible(true);
	//					frame.render(data);

						System.out.println("one_complete!");

					}
				}

			});

		}

		Thread.sleep(20000);
		System.out.println("empty topic!!");
		consumer.shutdown();
		executor.shutdown();
	}
	
	

}
