extern "C"
__global__ void multiplication(char* M, char* N, char* P, int Width)
{
    int tid, tx, ty;
	tx = blockDim.x*blockIdx.x + threadIdx.x;
	ty = blockDim.y*blockIdx.y + threadIdx.y;
	tid = Width*ty + tx;
       
  	char Value = 0;
	char MVal = 0;
	char NVal = 0;

	for (int i = 0; i < Width; i++)
	{
		MVal = M[ty * Width + i];
		NVal = N[i * Width + tx];
		Value += MVal * NVal;
	}

	P[tid] = Value;

}
