#ifndef _LINUX_MAJOR_H
#define _LINUX_MAJOR_H

/*
 * This file has definitions for major device numbers.
 * For the device number assignments, see Documentation/devices.txt.
 */

/* limits */

/*
 * Important: Don't change this to 256.  Major number 255 is and must be
 * reserved for future expansion into a larger dev_t space.
 */
#define MAX_CHRDEV	255
#define MAX_BLKDEV	255

#define UNNAMED_MAJOR	0
#define MEM_MAJOR	1
#define RAMDISK_MAJOR	1
#define FLOPPY_MAJOR	2
#define PTY_MASTER_MAJOR 2
#define IDE0_MAJOR	3
#define PTY_SLAVE_MAJOR 3
#define HD_MAJOR	IDE0_MAJOR
#define TTY_MAJOR	4
#define TTYAUX_MAJOR	5
#define LP_MAJOR	6
#define VCS_MAJOR	7
#define LOOP_MAJOR	7
#define SCSI_DISK0_MAJOR 8
#define SCSI_TAPE_MAJOR	9
#define MD_MAJOR        9
#define MISC_MAJOR	10
#define SCSI_CDROM_MAJOR 11
#define QIC02_TAPE_MAJOR 12
#define XT_DISK_MAJOR	13
#define SOUND_MAJOR	14
#define CDU31A_CDROM_MAJOR 15
#define JOYSTICK_MAJOR	15
#define GOLDSTAR_CDROM_MAJOR 16
#define OPTICS_CDROM_MAJOR 17
#define SANYO_CDROM_MAJOR 18
#define CYCLADES_MAJOR  19
#define CYCLADESAUX_MAJOR 20
#define MITSUMI_X_CDROM_MAJOR 20
#define MFM_ACORN_MAJOR 21	/* ARM Linux /dev/mfm */
#define SCSI_GENERIC_MAJOR 21
#define Z8530_MAJOR 34
#define DIGI_MAJOR 23
#define IDE1_MAJOR	22
#define DIGICU_MAJOR 22
#define MITSUMI_CDROM_MAJOR 23
#define CDU535_CDROM_MAJOR 24
#define STL_SERIALMAJOR 24
#define MATSUSHITA_CDROM_MAJOR 25
#define STL_CALLOUTMAJOR 25
#define MATSUSHITA_CDROM2_MAJOR 26
#define QIC117_TAPE_MAJOR 27
#define MATSUSHITA_CDROM3_MAJOR 27
#define MATSUSHITA_CDROM4_MAJOR 28
#define STL_SIOMEMMAJOR 28
#define ACSI_MAJOR	28
#define AZTECH_CDROM_MAJOR 29
#define GRAPHDEV_MAJOR	29	/* SparcLinux & Linux/68k /dev/fb */
#define CM206_CDROM_MAJOR 32
#define IDE2_MAJOR	33
#define IDE3_MAJOR	34
#define NETLINK_MAJOR	36
#define PS2ESDI_MAJOR	36
#define IDETAPE_MAJOR	37
#define Z2RAM_MAJOR	37
#define APBLOCK_MAJOR   38   /* AP1000 Block device */
#define DDV_MAJOR       39   /* AP1000 DDV block device */
#define NBD_MAJOR	43   /* Network block device	*/
#define RISCOM8_NORMAL_MAJOR 48
#define RISCOM8_CALLOUT_MAJOR 49
#define MKISS_MAJOR	55
#define DSP56K_MAJOR    55   /* DSP56001 processor device */

#define IDE4_MAJOR	56
#define IDE5_MAJOR	57
#define PDA_MAJOR       59

#define SCSI_DISK1_MAJOR	65
#define SCSI_DISK2_MAJOR	66
#define SCSI_DISK3_MAJOR	67
#define SCSI_DISK4_MAJOR	68
#define SCSI_DISK5_MAJOR	69
#define SCSI_DISK6_MAJOR	70
#define SCSI_DISK7_MAJOR	71

#define SPECIALIX_NORMAL_MAJOR 75
#define SPECIALIX_CALLOUT_MAJOR 76

#define UNIX98_PTY_MASTER_MAJOR	128
#define UNIX98_PTY_MAJOR_COUNT	8
#define UNIX98_PTY_SLAVE_MAJOR	(UNIX98_PTY_MASTER_MAJOR+UNIX98_PTY_MAJOR_COUNT)

/*
 * Tests for SCSI devices.
 */

#define SCSI_DISK_MAJOR(M) ((M) == SCSI_DISK0_MAJOR || \
  ((M) >= SCSI_DISK1_MAJOR && (M) <= SCSI_DISK7_MAJOR))
  
#define SCSI_BLK_MAJOR(M) \
  (SCSI_DISK_MAJOR(M)	\
   || (M) == SCSI_CDROM_MAJOR)

static __inline__ int scsi_blk_major(int m) {
	return SCSI_BLK_MAJOR(m);
}

#endif
