//
// Created by Admin on 2023/5/7.
//

#include "Utils.h"

#include <unistd.h>

#include <sys/file.h>

#include <sys/types.h>

#include <sys/stat.h>

#include <stdio.h>

#include <stdlib.h>
#include <jni.h>





int lock_set(int fd,int type)
{
    struct flock old_lock,lock;

    lock.l_whence = SEEK_SET;

    lock.l_start = 0;

    lock.l_len = 0;

    lock.l_type = type;

    lock.l_pid = -1;

    fcntl(fd,F_GETLK,&lock);

    if(lock.l_type != F_UNLCK)
    {
        if (lock.l_type == F_RDLCK)
        {
            printf("Read lock already set by %d\n",lock.l_pid);
        }
        else if (lock.l_type == F_WRLCK)
        {
            printf("Write lock already set by %d\n",lock.l_pid);
        }
    }

    lock.l_type = type;

    if ((fcntl(fd,F_SETLKW,&lock)) < 0)
    {
        printf("Lock failed : type = %d\n",lock.l_type);
        return 1;
    }

    switch (lock.l_type)
    {
        case F_RDLCK:
        {
            printf("Read lock set by %d\n",getpid());
        }
        break;

        case F_WRLCK:
        {
            printf("write lock set by %d\n",getpid());
        }
        break;

        case F_UNLCK:
        {
            printf("Release lock by %d\n",getpid());
            return 1;
        }
        break;

        default: {
            break;
        }
    }

    return 0;
}


extern "C" JNIEXPORT jint JNICALL Java_com_adobe_flashplayer_core_CoreHelper_fileLock(JNIEnv * env,jobject obj,jstring dstfn){

    jboolean iscopy = 1;
    char * dstfilename = (char*)env->GetStringUTFChars(dstfn,&iscopy);

    int fd = open(dstfilename,O_RDWR | O_CREAT, 0644);
    if(fd < 0)
    {
        printf("Open file error\n");
        return false;
    }

    lock_set(fd, F_WRLCK);

    return 0;
}
