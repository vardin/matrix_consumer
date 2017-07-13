package matrix_consumer;

import jcuda_matrix.jcuda_matrix;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import java.util.stream.IntStream;
import javax.swing.plaf.synth.SynthSeparatorUI;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

public class matrix_consumer {

	private static final String TOPIC = "supercom";
	private static final int NUM_THREADS = 3;

	// static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	public static void main(String[] args) throws Exception {

		// System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Properties props = new Properties();
		final jcuda_matrix jcuda_matrix = new jcuda_matrix(23);
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

		System.out.println("partial success!");

		for (final KafkaStream<byte[], byte[]> stream : streams) {   //for 문이 2개 들어가 있는게 문제~ thread pool을 1로 수정
			System.out.println("for start! 1");
			// MyFrame frame = new MyFrame(); 
			Runnable runnable = new Runnable() {
				public void run() {
					System.out.println("for start! 2 thread name : "+ Thread.currentThread().getName());
					for (MessageAndMetadata<byte[], byte[]> messageAndMetadata : stream) {
						System.out.println("for start! 3"); 
						byte[] test = messageAndMetadata.message();
						System.out.println(test.length);
						jcuda_matrix.prepare_cuda_memory(test);
						jcuda_matrix.cudaCleanUp();

						// Mat data = new Mat(480, 640, CvType.CV_8UC3);
						// data.put(0, 0, test);
						// frame.setVisible(true);
						// frame.render(data);
						
						System.out.println("one_complete! ");
					}
				}
			};
			executor.execute(runnable);
		}
		
		Thread.sleep(10000);
		System.out.println("empty topic!!");
		consumer.shutdown();
		executor.shutdown();
	}

}
