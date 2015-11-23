// PART OF THE MACHINE SIMULATION. DO NOT CHANGE.

package nachos.machine;

/**
 * A single translation between a virtual page and a physical page.
 */
public final class TranslationEntry {
    /**
     * Allocate a new invalid translation entry.
     */
    public TranslationEntry() {
        valid = false;
    }

    /**
     * Allocate a new translation entry with the specified initial state.
     *
     * @param vpn
     *            the virtual page numben.
     * @param ppn
     *            the physical page number.
     * @param valid
     *            the valid bit.
     * @param readOnly
     *            the read-only bit.
     * @param used
     *            the used bit.
     * @param dirty
     *            the dirty bit.
     * @param compressed
     *            the compressed bit
     * @param compressedBlock
     *            compressed block starting addr, used number of pages and vpn for each compressed
     *            page
     */
    public TranslationEntry(int vpn, int ppn, boolean valid, boolean readOnly,
            boolean used, boolean dirty, boolean compressed, int compressOffset,
            CompressMemBlock compressedMemBlock) {
        this.vpn = vpn;
        this.ppn = ppn;
        this.valid = valid;
        this.readOnly = readOnly;
        this.used = used;
        this.dirty = dirty;
        this.compressed = compressed;
        this.compressOffset = compressOffset;
        this.compressMemBlock = compressedMemBlock;
    }

    /**
     * Allocate a new translation entry, copying the contents of an existing one.
     *
     * @param entry
     *            the translation entry to copy.
     */
    public TranslationEntry(TranslationEntry entry) {
        vpn = entry.vpn;
        ppn = entry.ppn;
        valid = entry.valid;
        readOnly = entry.readOnly;
        used = entry.used;
        dirty = entry.dirty;
        compressed = compressed;
        compressOffset = compressOffset;
        compressMemBlock = compressMemBlock;
    }

    /** The virtual page number. */
    public int vpn;

    /** The physical page number. */
    public int ppn;

    /**
     * If this flag is <tt>false</tt>, this translation entry is ignored.
     */
    public boolean valid;

    /**
     * If this flag is <tt>true</tt>, the user pprogram is not allowed to modify the contents of
     * this virtual page.
     */
    public boolean readOnly;

    /**
     * This flag is set to <tt>true</tt> every time the page is read or written by a user program.
     */
    public boolean used;

    /**
     * This flag is set to <tt>true</tt> every time the page is written by a user program.
     */
    public boolean dirty;

    /**
     * This flag is set to <tt>true</tt> if the virtual page is in compressed format in memory.
     */
    public boolean compressed;

    /**
     * The offset of this page in compressed block.
     */
    public int compressOffset;

    /**
     * Store starting addr, number of pages it used and vpn for each compressed page
     */
    public CompressMemBlock compressMemBlock;
}
