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
	
	
	int width;
	int numElemnets;
	CUfunction function;
	int blockSizeX;
	int gridSizeX;
	Pointer kernelParameters;
	int hostInputA[];
	int hostInputB[];
	int hostOutput[];
	CUdeviceptr deviceInputA;
	CUdeviceptr deviceInputB;
	CUdeviceptr deviceOutput;
	
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

		this.width = input_width*input_width;
		numElemnets = (width) * (width);
		
	}
	
	public void prepare_cuda_memory()
	{
		// Allocate and fill the host input data
				hostInputA = new int[numElemnets];
				hostInputB = new int[numElemnets];
				hostOutput = new int[numElemnets];
				for (int i = 0; i < numElemnets; i++) {
					hostInputA[i] = (int) i;
					hostInputB[i] = (int) i;
				}

				// Allocate the device input data, and copy the
				// host input data to the device
				deviceInputA = new CUdeviceptr();
				cuMemAlloc(deviceInputA, numElemnets * Sizeof.INT);
				deviceInputB = new CUdeviceptr();
				cuMemAlloc(deviceInputB, numElemnets * Sizeof.INT);
				deviceOutput = new CUdeviceptr();
				cuMemAlloc(deviceOutput, numElemnets * Sizeof.INT);
		
				cuMemcpyHtoD(deviceInputA, Pointer.to(hostInputA), numElemnets * Sizeof.INT);

				cuMemcpyHtoD(deviceInputB, Pointer.to(hostInputB), numElemnets * Sizeof.INT);

				// Allocate device output memory

				// Set up the kernel parameters: A pointer to an array
				// of pointers which point to the actual values.

				kernelParameters = Pointer.to(Pointer.to(deviceInputA),
						Pointer.to(deviceInputB), Pointer.to(deviceOutput),Pointer.to(new int[] { width }));

				// Call the kernel function.

				blockSizeX = (int)Math.sqrt(width); //the number of thread
				// int gridSizeX = (int)Math.ceil((double)numElements / blockSizeX);
				gridSizeX = (int)Math.sqrt(width);	 //the number of block
		
	}
	
	
	public void cuda_execution(){
		
		cuLaunchKernel(function, gridSizeX, gridSizeX, 1, // Grid dimension //number of block
				blockSizeX, blockSizeX, 1, // Block dimension //number of thread
				0, null, // Shared memory size and stream
				kernelParameters, null // Kernel- and extra parameters
		);
		cuCtxSynchronize();

		// Allocate host output memory and copy the device output
		// to the host.

		cuMemcpyDtoH(Pointer.to(hostOutput), deviceOutput, numElemnets * Sizeof.INT);
		
		System.out.println(hostOutput[0]);
		// Verify the result
		
	}
	
	
	public void cudaCleanUp(){
		// Clean up.
		
		cuMemFree(deviceInputA);
		cuMemFree(deviceInputB);
		cuMemFree(deviceOutput);
		
	}
	
	
}
