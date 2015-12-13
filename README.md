# OSProject

## Authors

* Toan Luong
* Hsiang-Yu Huang
* Qian Huang
* Hongyu Cui

## Install Nachos
* Please follow instructions in following link to install Nachos 5.0j and cross-compiler
* http://www.cas.mcmaster.ca/~rzheng/course/Nachos_Tutorial/nachosse2.html


## Import Eclipse project
* File -> Import -> Existing Projects into Workspace -> root directory: OSProject/Eclipse
* The project use jdk 1.8, so please install jdk 1.8 first.

## Modified files
* machine folder
  * added CompressMemBlock.java
  * modified Processor.java: added findVictim(int) function
* userprog folder
  * added MemoryCompression.java
  * newly added functions in UserProcess.java
    * Processor.exceptionPageFault case in handleException()
    * getPageTableEntry()
    * readCompressMemory()
    * writeCompressMemory()
    * handlePageFault()
    * pageFaultHelper()
    * swapinIntoFreePhysMem()
    * swapinIntoVictims()
    * printPhysMemStatus()
    * printPageTable()
* test folder
  * added recursive_sum.c
  * added quick_sort.c

## Run test coff file in nachos
```bash
cd Eclipse/nachos/proj_mem_comp/
make
nachos -x echo.coff -a hello -a world [-d a]
```
* '-d a' is for debugging (optional)
* make sure nachos is in your PATH

## Run & debug nachos in Eclipse
* Create a Run configuration
* Main class: nachos.machine.Machine
* Program arguments: -x halt.coff -d a
* Working directory: ${workspace_loc:Eclipse/nachos/proj_mem_comp}
* Apply -> Run
