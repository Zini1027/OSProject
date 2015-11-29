#include "stdio.h"
#include "stdlib.h"

int sum(int n)
{
	if (n == 1)
	{
		return n;
	}
	else
	{
		return n + sum(n - 1);
	}
}

int main(int argc, char** argv)
{
  // Calculate 1+2+3+4+...+n
  int n;
  if (argc != 2) {
  	printf("Usage: factor 10\n");
  	return 0;
  } else {
  	n = atoi(argv[1]);
  	printf("%d sum is %d\n", n, sum(n));
  }
  return 0;
}
