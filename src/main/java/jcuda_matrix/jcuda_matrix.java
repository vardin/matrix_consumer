package jcuda_matrix;

import static jcuda.driver.JCudaDriver.cuCtxCreate;
import static jcuda.driver.JCudaDriver.cuCtxSynchronize;
import static jcuda.driver.JCudaDriver.cuDeviceGet;
import static jcuda.driver.JCudaDriver.cuInit;
import static jcuda.driver.JCudaDriver.cuLaunchKernel;
import static jcuda.driver.JCudaDriver.cuMemAlloc;
import static jcuda.driver.JCudaDriver.cuMemFree;
import static jcuda.driver.JCudaDriver.cuMemcpyDtoH;
import static jcuda.driver.JCudaDriver.cuMemcpyHtoD;
import static jcuda.driver.JCudaDriver.cuModuleGetFunction;
import static jcuda.driver.JCudaDriver.cuModuleLoad;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUcontext;
import jcuda.driver.CUdevice;
import jcuda.driver.CUdeviceptr;
import jcuda.driver.CUfunction;
import jcuda.driver.CUmodule;
import jcuda.driver.JCudaDriver;
import jcuda.samples.utils.*;

public class jcuda_matrix {
		
	static int width;
	static int block_gridsize;
	static int numElements;
	static CUfunction function;
	int blockSizeX;
	int gridSizeX;
	static Pointer kernelParameters;
	static byte hostInputA[];
	static byte hostInputB[];
	static byte hostOutput[];
	static CUdeviceptr deviceInputA;
	static CUdeviceptr deviceInputB;
	static CUdeviceptr deviceOutput;
	
	public jcuda_matrix(int input_width){
			
		// Enable exceptions and omit all subsequent error checks
		JCudaDriver.setExceptionsEnabled(true);

		// Create the PTX file by calling the NVCC
		String ptxFileName = JCudaSamplesUtils
				.preparePtxFile("src/main/resources/kernels/JCudaVectorMatrixMultiplication.cu");

		// Initialize the driver and create a context for the first device.
		cuInit(0);
			
		CUdevice device = new CUdevice();
		cuDeviceGet(device, 0);
		CUcontext context = new CUcontext();
		cuCtxCreate(context, 0, device);

		// Load the ptx file.
		CUmodule module = new CUmodule();
		cuModuleLoad(module, ptxFileName);

		// Obtain a function pointer to the "add" function.
		function = new CUfunction();
		cuModuleGetFunction(function, module, "multiplication");
		this.block_gridsize= input_width;
		this.width = input_width*input_width;
		numElements = (width) * (width);
		
		hostInputA = new byte[numElements];
		hostInputB = new byte[numElements];
		hostOutput= new byte[numElements];
		
		System.out.println("constructor is finished! " + numElements);
		
		
		this.deviceInputA = new CUdeviceptr();
		cuMemAlloc(deviceInputA, numElements * Sizeof.BYTE);
		this.deviceInputB = new CUdeviceptr();
		cuMemAlloc(deviceInputB, numElements * Sizeof.BYTE);
		this.deviceOutput = new CUdeviceptr();
		cuMemAlloc(deviceOutput, numElements * Sizeof.BYTE);
		
	}
	
	public static void prepare_cuda_memory(byte[] InputA)
	{
	// Allocate and fill the host input data

		/*
		 * 
		 * for (int i = 0; i < numElemnets; i++) { hostInputA[i] = (int) i;
		 * hostInputB[i] = (int) i; }
		 * 
		 */

		// Allocate the device input data, and copy the
		// host input data to the device
			
				hostInputA = InputA.clone();
				hostInputB = InputA.clone();
				
				System.out.println("this is before cuMemcpyHtoD! = "+hostInputA.length);
			
				cuMemcpyHtoD(deviceInputA, Pointer.to(hostInputA), numElements * Sizeof.BYTE);

				cuMemcpyHtoD(deviceInputB, Pointer.to(hostInputB), numElements * Sizeof.BYTE);
				
				System.out.println("this is after cuMemcpyHtoD!");
				// Allocate device output memory

				// Set up the kernel parameters: A pointer to an array
				// of pointers which point to the actual values.
				
				
				
				
				kernelParameters = Pointer.to(Pointer.to(deviceInputA),
						Pointer.to(deviceInputB), Pointer.to(deviceOutput),Pointer.to(new int[] { width }));

//					blockSizeX = this.block_gridsize; //the number of thread
				// int gridSizeX = (int)Math.ceil((double)numElements / blockSizeX);
//					gridSizeX = this.block_gridsize;	 //the number of block
				
				cuLaunchKernel(function, 23, 23, 1, // Grid dimension //number of block
						23, 23, 1, // Block dimension //number of thread
						0, null, // Shared memory size and stream
						kernelParameters, null // Kernel- and extra parameters
				);

				System.out.println("this is after CUlaunchkernel");
			

			
				
				// Allocate host output memory and copy the device output
				// to the host.

				cuMemcpyDtoH(Pointer.to(hostOutput), deviceOutput, numElements * Sizeof.BYTE);
				
				System.out.println("this is after CUMemcpyDtoH");
				
				System.out.println(hostOutput[0]);
				// Verify the result
				cuCtxSynchronize();

		
	}
	
	

	
	public void cudaCleanUp(){
				
		// Clean up.
		cuMemFree(deviceInputA);
		cuMemFree(deviceInputB);
		cuMemFree(deviceOutput);
		
	}
	
	
}
