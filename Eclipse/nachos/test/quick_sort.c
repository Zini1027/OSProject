#include "stdio.h"

#define SIZE (1024 * 8)

void quicksort(int *,int,int);

int main(){
  int x[SIZE],i;
  for (i = 0;i<SIZE;i++) {
    x[i] = SIZE - i;
  }
  printf("Start to sort!");
  quicksort(x,0,SIZE-1);
  printf("Sorted!");
  return 0;
}

void quicksort(int* x,int first,int last){
    int pivot,j,temp,i;

     if(first<last){
         pivot=first;
         i=first;
         j=last;

         while(i<j){
             while(x[i]<=x[pivot]&&i<last)
                 i++;
             while(x[j]>x[pivot])
                 j--;
             if(i<j){
                 temp=x[i];
                  x[i]=x[j];
                  x[j]=temp;
             }
         }

         temp=x[pivot];
         x[pivot]=x[j];
         x[j]=temp;
         quicksort(x,first,j-1);
         quicksort(x,j+1,last);

    }
}
