/* sort.c 
 *    Test program to sort a large number of integers.
 *
 *    Intention is to stress virtual memory system. To increase the memory
 *    usage of this program, simply increase SORTSHIFT. The size of the array
 *    is (SORTSIZE)(2^(SORTSHIFT+2)).
 */

#include "syscall.h"

/* size of physical memory; with code, we'll run out of space! */
#define SORTSIZE	256
#define SORTSHIFT	4

#define	A(i)	(array[(i)<<SORTSHIFT])

void swap(int* x, int* y)
{
  int temp = *x;
  *x = *y;
  *y = temp;
}

int
main()
{
  int i, j;
  int array[SORTSIZE<<SORTSHIFT];
  
  printf("sort starts!\n");
  /* first initialize the array, in reverse sorted order */
  for (i=0; i<SORTSIZE; i++)
    A(i) = (SORTSIZE-1)-i;

  /* then sort! */
  for (i=0; i<SORTSIZE-1; i++) {
    for (j=i; j<SORTSIZE; j++) {
      if (A(i) > A(j))
	swap(&A(i), &A(j));
    }
  }

  printf("sort done!\n");
  /* and last, verify */
  for (i=0; i<SORTSIZE; i++) {
    if (A(i) != i)
      printf("sort is wrong!\n");
      return 0;
  }

  printf("sort is successful!\n");
  /* if successful, return 0 */
  return 0;
}
