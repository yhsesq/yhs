#ifndef _LINUX_PDA_H
#define _LINUX_PDA_H

#include <linux/kdev_t.h>

/*
 * include/linux/pda.h
 *
 * Written by Zurk Tech, 12/30/98.
 *
 * Copyright 1998 by Zurk Tech.  Redistribution of this file is
 * permitted under the GNU Public License.
 */

#define PDA_NAME_SIZE	64

#ifdef __KERNEL__
       
struct pda_device {
	int		pda_number;
	struct dentry	*pda_dentry;
	int		pda_refcnt;
	kdev_t		pda_device;
	int		pda_offset;
//	int		pda_flags;
	char		pda_name[PDA_NAME_SIZE];
	__u32           pda_init[2];
//	int		(*ioctl)(struct pda_device *, int cmd, 
//				 unsigned long arg); 
};

#endif /* __KERNEL__ */
/* 
 * Note that this structure gets the wrong offsets when directly used
 * from a glibc program, because glibc has a 32bit dev_t.
 * Prevent people from shooting in their own foot.  
 */

#if __GLIBC__ >= 2 && !defined(dev_t)
#error "Wrong dev_t in loop.h"
#endif 

/*
 *	This uses kdev_t because glibc currently has no appropiate
 *	conversion version for the loop ioctls. 
 * 	The situation is very unpleasant	
 */

//  struct pda_info {
//	int		pda_number;	/* ioctl r/o */
//	dev_t		pda_device; 	/* ioctl r/o */
//	unsigned long	pda_inode; 	/* ioctl r/o */
//	dev_t		pda_rdevice; 	/* ioctl r/o */
//	int		pda_offset;
//	int		pda_flags;	/* ioctl r/o */
//	char		pda_name[PDA_NAME_SIZE];
//	unsigned long	pda_init[2];
//	char		reserved[4];
// };

#endif
