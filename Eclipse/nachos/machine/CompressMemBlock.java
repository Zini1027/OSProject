/*************************************************************************
 *
 * Copyright (c) 2015, DATAVISOR, INC.
 * All rights reserved.
 * __________________
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of DataVisor, Inc.
 * The intellectual and technical concepts contained
 * herein are proprietary to DataVisor, Inc. and
 * may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from DataVisor, Inc.
 */

package nachos.machine;

import java.util.ArrayList;
import java.util.List;

public class CompressMemBlock {

    public CompressMemBlock() {

    }

    public CompressMemBlock(int ppn, int byteUsed) {
        this.startPPN = ppn;
        this.compressedByte = byteUsed;
    }

    public int getStartAddr() {
        return startPPN;
    }

    public void setStartAddr(int startPPN) {
        this.startPPN = startPPN;
    }

    public int getCompressedByte() {
        return compressedByte;
    }

    public void setCompressedByte(int compressedByte) {
        this.compressedByte = compressedByte;
    }

    public int getUnCompressedByte() {
        return unCompressedByte;
    }

    public void setUnCompressedByte(int unCompressedByte) {
        this.unCompressedByte = unCompressedByte;
    }

    public int getUncompressedPageNum() {
        return (unCompressedByte / Processor.pageSize)
                + (unCompressedByte % Processor.pageSize == 0 ? 0 : 1);
    }

    /**
     * For process initialization add continuous vpn to vpnList
     * */
    public void setVPNList(List<Integer> vpns) {
        vpnList.addAll(vpns);
    }

    public int getVPN(int offset) {
        return vpnList.get(offset);
    }

    // check number of vpn == number of uncompressed page
    public boolean isVPNListSet() {
        return (vpnList.size() == getUncompressedPageNum());
    }

    /** The compressed memory start addr. */
    public int startPPN;

    /** Number of byte in the compressed block. */
    public int compressedByte;

    public int unCompressedByte;

    /**
     * vpn for each page in compressed block
     */
    public List<Integer> vpnList = new ArrayList<Integer>();

}
