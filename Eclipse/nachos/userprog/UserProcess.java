package nachos.userprog;

import java.io.EOFException;

import nachos.machine.Coff;
import nachos.machine.CoffSection;
import nachos.machine.CompressMemBlock;
import nachos.machine.Lib;
import nachos.machine.Machine;
import nachos.machine.OpenFile;
import nachos.machine.Processor;
import nachos.machine.TranslationEntry;
import nachos.threads.ThreadedKernel;

/**
 * Encapsulates the state of a user process that is not contained in its user thread (or threads).
 * This includes its address translation state, a file table, and information about the program
 * being executed.
 *
 * <p>
 * This class is extended by other classes to support additional functionality (such as additional
 * syscalls).
 *
 * @see nachos.vm.VMProcess
 * @see nachos.network.NetProcess
 */
public class UserProcess {
    /**
     * Allocate a new process.
     */
    public UserProcess() {
        int numPhysPages = Machine.processor().getNumPhysPages();

        pageTable = new TranslationEntry[numVirtualPages];

    }

    /**
     * Allocate and return a new process of the correct class. The class name is specified by the
     * <tt>nachos.conf</tt> key <tt>Kernel.processClassName</tt>.
     *
     * @return a new process of the correct class.
     */
    public static UserProcess newUserProcess() {
        return (UserProcess) Lib.constructObject(Machine.getProcessClassName());
    }

    /**
     * Execute the specified program with the specified arguments. Attempts to load the program, and
     * then forks a thread to run it.
     *
     * @param name
     *            the name of the file containing the executable.
     * @param args
     *            the arguments to pass to the executable.
     * @return <tt>true</tt> if the program was successfully executed.
     */
    public boolean execute(String name, String[] args) {
        if (!load(name, args))
            return false;

        new UThread(this).setName(name).fork();

        return true;
    }

    /**
     * Save the state of this process in preparation for a context switch. Called by
     * <tt>UThread.saveState()</tt>.
     */
    public void saveState() {
    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {
        Machine.processor().setPageTable(pageTable);
    }

    /**
     * Read a null-terminated string from this process's virtual memory. Read at most
     * <tt>maxLength + 1</tt> bytes from the specified address, search for the null terminator, and
     * convert it to a <tt>java.lang.String</tt>, without including the null terminator. If no null
     * terminator is found, returns <tt>null</tt>.
     *
     * @param vaddr
     *            the starting virtual address of the null-terminated string.
     * @param maxLength
     *            the maximum number of characters in the string, not including the null terminator.
     * @return the string read, or <tt>null</tt> if no null terminator was found.
     */
    public String readVirtualMemoryString(int vaddr, int maxLength) {
        Lib.assertTrue(maxLength >= 0);

        byte[] bytes = new byte[maxLength + 1];

        int bytesRead = readVirtualMemory(vaddr, bytes);

        for (int length = 0; length < bytesRead; length++) {
            if (bytes[length] == 0)
                return new String(bytes, 0, length);
        }

        return null;
    }

    /**
     * Transfer data from this process's virtual memory to all of the specified array. Same as
     * <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param vaddr
     *            the first byte of virtual memory to read.
     * @param data
     *            the array where the data will be stored.
     * @return the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data) {
        return readVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from this process's virtual memory to the specified array. This method handles
     * address translation details. This method must <i>not</i> destroy the current process if an
     * error occurs, but instead should return the number of bytes successfully copied (or zero if
     * no data could be copied).
     *
     * @param vaddr
     *            the first byte of virtual memory to read.
     * @param data
     *            the array where the data will be stored.
     * @param offset
     *            the first byte to write in the array.
     * @param length
     *            the number of bytes to transfer from virtual memory to the array.
     * @return the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data, int offset,
            int length) {
        Lib.assertTrue(offset >= 0 && length >= 0 && offset + length <= data.length);

        byte[] memory = Machine.processor().getMemory();
        int firstVPN = Processor.pageFromAddress(vaddr);
        int firstOffset = Processor.offsetFromAddress(vaddr);
        int lastVPN = Processor.pageFromAddress(vaddr + length);

        TranslationEntry entry = getPageTableEntry(firstVPN, false);

        if (entry == null) {
            return 0;
        }

        // load the first page ( not start at offset=0)
        int amount = Math.min(length, pageSize - firstOffset);
        System.arraycopy(memory, Processor.makeAddress(entry.ppn, firstOffset),
                data, offset, amount);
        offset += amount;

        for (int i = firstVPN + 1; i <= lastVPN; i++) {
            entry = getPageTableEntry(i, false);
            if (entry == null)
                return amount;
            int len = Math.min(length - amount, pageSize);
            System.arraycopy(memory, Processor.makeAddress(entry.ppn, 0), data,
                    offset, len);
            offset += len;
            amount += len;
        }

        return amount;
    }

    /**
     * Transfer all data from the specified array to this process's virtual memory. Same as
     * <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param vaddr
     *            the first byte of virtual memory to write.
     * @param data
     *            the array containing the data to transfer.
     * @return the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data) {
        return writeVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from the specified array to this process's virtual memory. This method handles
     * address translation details. This method must <i>not</i> destroy the current process if an
     * error occurs, but instead should return the number of bytes successfully copied (or zero if
     * no data could be copied).
     *
     * @param vaddr
     *            the first byte of virtual memory to write.
     * @param data
     *            the array containing the data to transfer.
     * @param offset
     *            the first byte to transfer from the array.
     * @param length
     *            the number of bytes to transfer from the array to virtual memory.
     * @return the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data, int offset,
            int length) {
        Lib.assertTrue(offset >= 0 && length >= 0 && offset + length <= data.length);

        byte[] memory = Machine.processor().getMemory();

        int firstVPN = Processor.pageFromAddress(vaddr);
        int firstOffset = Processor.offsetFromAddress(vaddr);
        int lastVPN = Processor.pageFromAddress(vaddr + length);

        TranslationEntry entry = pageTable[firstVPN];

        if (entry == null) {
            return 0;
        }

        int amount = Math.min(length, pageSize - firstOffset);
        System.arraycopy(data, offset, memory, Processor.makeAddress(entry.ppn,
                firstOffset), amount);
        offset += amount;

        for (int i = firstVPN + 1; i <= lastVPN; i++) {
            entry = getPageTableEntry(i, true);
            if (entry == null)
                return amount;
            int len = Math.min(length - amount, pageSize);
            System.arraycopy(data, offset, memory, Processor.makeAddress(
                    entry.ppn, 0), len);
            offset += len;
            amount += len;
        }

        return amount;
    }

    protected TranslationEntry getPageTableEntry(int vpn, boolean isWrite) {
        // out of virtual memory range
        if (vpn < 0 || vpn >= numPages) {
            return null;
        }

        TranslationEntry entry = pageTable[vpn];
        // non-initialized page table entry
        if (entry == null) {
            return null;
        }

        // write to readOnly page
        if (entry.readOnly && isWrite) {
            return null;
        }
        // set page to used
        entry.used = true;

        // if write, set dirty bit true
        if (isWrite)
            entry.dirty = true;

        return entry;
    }

    @Deprecated
    protected int translateVaddrToPaddr(int vaddr) {
        int vPageNumber = Processor.pageFromAddress(vaddr);
        int pageAddress = Processor.offsetFromAddress(vaddr);
        if (null == pageTable) {
            System.out.println("pageTable is not initialized");
            return -1;
        }
        if (vPageNumber >= pageTable.length) {
            System.out.println("Requested a page number (" + vPageNumber
                    + ") outside the page mapping (" + pageTable.length + " total)");
            return -1;
        }
        TranslationEntry translationEntry = pageTable[vPageNumber];
        if (null == translationEntry) {
            // TODO: bus error? page fault?
            System.out.println("Unmapped page table entry for VPN " + vPageNumber);
            return -1;
        }
        // TODO: validity check?
        int pPageNumber = translationEntry.ppn;

        if (pageAddress < 0 || pageAddress >= Processor.pageSize) {
            System.out.println("bogus pageAddress: " + pageAddress);
            return -1;
        }

        return Processor.makeAddress(pPageNumber, pageAddress);
    }

    /**
     * Load the executable with the specified name into this process, and prepare to pass it the
     * specified arguments. Opens the executable, reads its header information, and copies sections
     * and arguments into this process's virtual memory.
     *
     * @param name
     *            the name of the file containing the executable.
     * @param args
     *            the arguments to pass to the executable.
     * @return <tt>true</tt> if the executable was successfully loaded.
     */
    private boolean load(String name, String[] args) {
        Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");

        OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
        if (executable == null) {
            Lib.debug(dbgProcess, "\topen failed");
            return false;
        }

        try {
            coff = new Coff(executable);
        } catch (EOFException e) {
            executable.close();
            Lib.debug(dbgProcess, "\tcoff load failed");
            return false;
        }

        // make sure the sections are contiguous and start at page 0
        numPages = 0;
        for (int s = 0; s < coff.getNumSections(); s++) {
            CoffSection section = coff.getSection(s);
            if (section.getFirstVPN() != numPages) {
                coff.close();
                Lib.debug(dbgProcess, "\tfragmented executable");
                return false;
            }
            numPages += section.getLength();
        }

        // make sure the argv array will fit in one page
        byte[][] argv = new byte[args.length][];
        int argsSize = 0;
        for (int i = 0; i < args.length; i++) {
            argv[i] = args[i].getBytes();
            // 4 bytes for argv[] pointer; then string plus one for null byte
            argsSize += 4 + argv[i].length + 1;
        }
        if (argsSize > pageSize) {
            coff.close();
            Lib.debug(dbgProcess, "\targuments too long");
            return false;
        }

        // program counter initially points at the program entry point
        initialPC = coff.getEntryPoint();

        // next comes the stack; stack pointer initially points to top of it
        numPages += stackPages;
        initialSP = numPages * pageSize;

        // and finally reserve 1 page for arguments
        numPages++;

        // if loadSection successfully, continue.
        if (!loadSections())
            return false;

        // store arguments in last page
        int entryOffset = (numPages - 1) * pageSize;
        int stringOffset = entryOffset + args.length * 4;

        this.argc = args.length;
        this.argv = entryOffset;

        // Load arguments
        for (int i = 0; i < argv.length; i++) {
            byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
            // write the
            Lib.assertTrue(writeVirtualMemory(entryOffset, stringOffsetBytes) == 4);
            entryOffset += 4;
            Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) ==
                    argv[i].length);
            stringOffset += argv[i].length;
            Lib.assertTrue(writeVirtualMemory(stringOffset, new byte[] { 0 }) == 1);
            stringOffset += 1;
        }

        return true;
    }

    /**
     * Allocates memory for this process, and loads the COFF sections into memory. If this returns
     * successfully, the process will definitely be run (this is the last step in process
     * initialization that can fail).
     *
     * @return <tt>true</tt> if the sections were successfully loaded.
     */
    protected boolean loadSections() {
        if (numPages > Machine.processor().getNumPhysPages()) {
            coff.close();
            Lib.debug(dbgProcess, "\tinsufficient physical memory");
            return false;
        }

        // initialize first numPages entries in page table
        // put code, stack, and argument in uncompressed memory
        for (int i = 0; i < numPages; i++) {
            pageTable[i] = new TranslationEntry(i, i, true, false,
                    false, false, false, -1, null);
        }

        // load sections
        for (int s = 0; s < coff.getNumSections(); s++) {
            CoffSection section = coff.getSection(s);

            Lib.debug(dbgProcess, "\tinitializing " + section.getName()
                    + " section (" + section.getLength() + " pages)");

            for (int i = 0; i < section.getLength(); i++) {
                int vpn = section.getFirstVPN() + i;
                int ppn = 0; // (TODO) get it from memory allocation function
                pageTable[vpn] = new TranslationEntry(vpn, ppn, true, section
                        .isReadOnly(), false, false, false, -1, null);
                // load page to physical memory
                section.loadPage(i, pageTable[vpn].ppn);
            }
        }

        // allocate memory for stack and arguments
        for (int i = numPages - stackPages - 1; i < numPages; i++) {
            int ppn = 0; // (TODO) get it from memory allocation function
            pageTable[i] = new TranslationEntry(i, ppn, true, false, false,
                    false, false, -1, null);
        }
        return true;
    }

    @Deprecated
    public boolean loadSectionToCombinedMem() {
        int numPhysicalPages = Machine.processor().getNumPhysPages();
        int numUncompressedPages = numPhysicalPages / (memoryDivideRatio + 1);
        int numCompressedPages = numPhysicalPages - numUncompressedPages;
        int compressMemStartPPN = numUncompressedPages;
        boolean uncompressedMemFull = false;
        boolean compressedMemFull = false;

        pageTable = new TranslationEntry[numPages];

        for (int s = 0; s < coff.getNumSections(); s++) {
            CoffSection section = coff.getSection(s);
            int sectionLen = section.getLength();

            Lib.debug(dbgProcess, "\tinitializing " + section.getName()
                    + " section (" + section.getLength() + " pages)");

            int availUncompressedPages = 5; // (TODO) get it from memory usage tracking
                                            // functions
            if (!uncompressedMemFull && (availUncompressedPages - sectionLen > numReservedPages)) {
                // load to uncompressed memory
                for (int i = 0; i < sectionLen; i++) {
                    int vpn = section.getFirstVPN() + i;
                    int ppn = 0; // (TODO) get ppn from memory usage tracking function
                    pageTable[vpn] = new TranslationEntry(vpn, ppn, true, true,
                            false, false, false, -1, null);
                    // load page to physical memory
                    section.loadPage(i, ppn);
                }
                continue;
            }
            System.out.println("Loaded " + (numUncompressedPages - availUncompressedPages)
                    + " pages to uncompressed memory");

            uncompressedMemFull = true;

            // (TODO) load section to compressed memory
            int availCompressedPages = 10; // (TODO) get it from memory usage function
            // get number of pages for compression
            byte[] compressBuf = new byte[compressedBlockPages * pageSize];

            // starting section page offset for compressionBlockPages
            int spn = 0;
            if (!compressedMemFull && (availCompressedPages - sectionLen > numReservedPages)) {
                for (int i = 0; i < sectionLen; i += compressedBlockPages) {
                    int vpn = section.getFirstVPN() + i;
                    // if left number of pages in section < default compressedBlockPages
                    int numPageLoadToBuf = Math.min(compressedBlockPages, sectionLen - i);

                    section.loadPagesToCompressBuf(spn, numPageLoadToBuf, compressBuf);
                    spn += numPageLoadToBuf;

                    // (TODO) call compression function, return byte array of compressed data
                    byte[] compressedBlock = null;
                    int pageUsed = (compressedBlock.length / pageSize)
                            + (compressedBlock.length % pageSize == 0 ? 0 : 1);

                    // (TODO) get available consecutive pages in compressed memory, return the first
                    // ppn
                    // how about if cannot find consecutive pages
                    int ppn = 0;
                    if (ppn == -1) {
                        System.out.println("No "
                                + pageUsed + " consecutive pages in compressed memory available.");
                        break;
                    }

                    // create CompressMemBlock
                    CompressMemBlock cmb = new CompressMemBlock();
                    cmb.startAddr = ppn * pageSize;
                    cmb.compressedByte = compressedBlock.length;
                    cmb.setVPNList(vpn, numPageLoadToBuf);

                    // Add pageTable entries for pages in compressed page block
                    for (int j = 0; j < numPageLoadToBuf; j++) {
                        ppn += j;
                        pageTable[vpn] = new TranslationEntry(vpn, ppn, true, true,
                                false, false, true, j, cmb);
                        vpn++;
                    }

                    // load compressed page to physical memory
                    Lib.assertTrue(writeVirtualMemory(cmb.startAddr, compressedBlock) == cmb.compressedByte);
                }
            } else {
                compressedMemFull = true;
                System.out.println("Load compressed memory error: Not enough space");
                return false;
            }
        }
        return true;
    }

    /**
     * Release any resources allocated by <tt>loadSections()</tt>.
     */
    protected void unloadSections() {
    }

    /**
     * Initialize the processor's registers in preparation for running the program loaded into this
     * process. Set the PC register to point at the start function, set the stack pointer register
     * to point at the top of the stack, set the A0 and A1 registers to argc and argv, respectively,
     * and initialize all other registers to 0.
     */
    public void initRegisters() {
        Processor processor = Machine.processor();

        // by default, everything's 0
        for (int i = 0; i < processor.numUserRegisters; i++)
            processor.writeRegister(i, 0);

        // initialize PC and SP according
        processor.writeRegister(Processor.regPC, initialPC);
        processor.writeRegister(Processor.regSP, initialSP);

        // initialize the first two argument registers to argc and argv
        processor.writeRegister(Processor.regA0, argc);
        processor.writeRegister(Processor.regA1, argv);
    }

    /**
     * Handle the halt() system call.
     */
    private int handleHalt() {

        Machine.halt();

        Lib.assertNotReached("Machine.halt() did not halt machine!");
        return 0;
    }

    private static final int
            syscallHalt = 0,
            syscallExit = 1,
            syscallExec = 2,
            syscallJoin = 3,
            syscallCreate = 4,
            syscallOpen = 5,
            syscallRead = 6,
            syscallWrite = 7,
            syscallClose = 8,
            syscallUnlink = 9;

    /**
     * Handle a syscall exception. Called by <tt>handleException()</tt>. The <i>syscall</i> argument
     * identifies which syscall the user executed:
     *
     * <table>
     * <tr>
     * <td>syscall#</td>
     * <td>syscall prototype</td>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td><tt>void halt();</tt></td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td><tt>void exit(int status);</tt></td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td><tt>int  exec(char *name, int argc, char **argv);
     * 								</tt></td>
     * </tr>
     * <tr>
     * <td>3</td>
     * <td><tt>int  join(int pid, int *status);</tt></td>
     * </tr>
     * <tr>
     * <td>4</td>
     * <td><tt>int  creat(char *name);</tt></td>
     * </tr>
     * <tr>
     * <td>5</td>
     * <td><tt>int  open(char *name);</tt></td>
     * </tr>
     * <tr>
     * <td>6</td>
     * <td><tt>int  read(int fd, char *buffer, int size);
     * 								</tt></td>
     * </tr>
     * <tr>
     * <td>7</td>
     * <td><tt>int  write(int fd, char *buffer, int size);
     * 								</tt></td>
     * </tr>
     * <tr>
     * <td>8</td>
     * <td><tt>int  close(int fd);</tt></td>
     * </tr>
     * <tr>
     * <td>9</td>
     * <td><tt>int  unlink(char *name);</tt></td>
     * </tr>
     * </table>
     * 
     * @param syscall
     *            the syscall number.
     * @param a0
     *            the first syscall argument.
     * @param a1
     *            the second syscall argument.
     * @param a2
     *            the third syscall argument.
     * @param a3
     *            the fourth syscall argument.
     * @return the value to be returned to the user.
     */
    public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
        switch (syscall) {
        case syscallHalt:
            return handleHalt();

        default:
            Lib.debug(dbgProcess, "Unknown syscall " + syscall);
            Lib.assertNotReached("Unknown system call!");
        }
        return 0;
    }

    /**
     * Handle a user exception. Called by <tt>UserKernel.exceptionHandler()</tt>. The <i>cause</i>
     * argument identifies which exception occurred; see the <tt>Processor.exceptionZZZ</tt>
     * constants.
     *
     * @param cause
     *            the user exception that occurred.
     */
    public void handleException(int cause) {
        Processor processor = Machine.processor();

        switch (cause) {
        case Processor.exceptionSyscall:
            int result = handleSyscall(processor.readRegister(Processor.regV0),
                    processor.readRegister(Processor.regA0),
                    processor.readRegister(Processor.regA1),
                    processor.readRegister(Processor.regA2),
                    processor.readRegister(Processor.regA3)
                    );
            processor.writeRegister(Processor.regV0, result);
            processor.advancePC();
            break;
        case Processor.exceptionPageFault:
            // (TODO) HandlePageFaultException
            // call swap funciton
            break;
        default:
            Lib.debug(dbgProcess, "Unexpected exception: " +
                    Processor.exceptionNames[cause]);
            Lib.assertNotReached("Unexpected exception");
        }
    }

    /** The program being run by this process. */
    protected Coff coff;

    /** This process's page table. */
    protected TranslationEntry[] pageTable;
    /** The number of contiguous pages occupied by the program. */
    protected int numPages;

    /** The number of pages in the program's stack. */
    protected final int stackPages = 8;

    private int initialPC, initialSP;
    private int argc, argv;

    private static final int pageSize = Processor.pageSize;
    private static final char dbgProcess = 'a';

    private static boolean enableCompressionMemory = false;

    private static int numVirtualPages = 256;

    // uncompressed memory section : compressed memory section
    private static final int memoryDivideRatio = 1;

    // number of pages to compressed together
    private static final int compressedBlockPages = 4;

    // leave some empty pages in both uncompressed and compressed memory for initialization
    private static final int numReservedPages = 4;

}
