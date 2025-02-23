
/* Syscall Prototypes for linux */

#define exit syscall_exit
#define open syscall_open
#define select syscall_select
#define mmap syscall_mmap
#define mremap syscall_mremap
#define munmap syscall_munmap
 
static _syscall1(int,exit,int,exitcode);
static _syscall0(pid_t,fork)
static _syscall3(int,write,int,fd,const void *,buffer,int,size)
static _syscall3(int,open,const char *,file,int,flag,int,mode)
static _syscall1(int,close,int,fd)
static _syscall1(int,unlink,const char *,file)
static _syscall1(int,select,const void *,args)
static _syscall1(int,mmap,const void *,args)
static _syscall2(int,munmap,const void *,buffer,int,size)
static _syscall3(int,waitpid,int,pid,int *,wait_stat,int,options)
static _syscall3(int,execve,const char *,file,char **,argv,char **,envp)
static _syscall0(pid_t,getpid)

/* 
 * Let's have a simple crt0 code, 
 * stolen from libc with some modifications.
 */

static void crt_stub( void ) 
{
    __asm__ __volatile__ ("\
    .globl _start\n \
    .globl _exit\n \
    .globl mapcompressed\n \
    .bss\n \
    mapcompressed:\n \
    .text\n \
    _start:	popl %ecx\n \
	movl %esp,%ebx      /* Points to the arguments */\n \
	movl %esp,%eax\n \
	andl $0xfffffff8,%esp\n \
	movl %ecx,%edx\n \
	addl %edx,%edx\n \
	addl %edx,%edx\n \
	addl %edx,%eax\n \
	addl $4,%eax\n      /* Points to the environment */\n \
    movl %eax,environ\n \
	push %ebx           /* Argument pointer */\n \
	pushl %ecx          /* And the argument count */\n \
    call main\n \
	_exit:  movl  %eax,%ebx\n \
	movl $1,%eax\n \
	int  $0x80" );
}
