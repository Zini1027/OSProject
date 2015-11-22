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

public class CompressMemBlock {

    public CompressMemBlock(int addr, int bytyUsed) {
        this.startAddr = addr;
        this.byteUsed = byteUsed;
    }

    /** The compressed memory start addr. */
    public int startAddr;

    /** Number of byte in the compressed block. */
    public int byteUsed;

    /**
     * vpn for each page in compressed block
     */
    public int[] vpnList;

}
