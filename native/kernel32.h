#ifndef KERNEL_TEST
#define KERNEL_TEST

#include <stdio.h>
#include <stdlib.h>

#ifdef __cplusplus
extern "C"
{
#endif


typedef char byte;

typedef unsigned short SHORT;
typedef unsigned short WCHAR;
typedef unsigned short WORD;
typedef void *HANDLE;
typedef unsigned int DWORD;
typedef unsigned long PTR;
typedef void *LPVOID;
typedef unsigned int BOOL;

typedef union _COMPOSITE_CHAR {
  WCHAR UnicodeChar;
  char AsciiChar;
} CompositeChar;

typedef struct _CHAR_INFO {
  union {
    WCHAR UnicodeChar;
    char  AsciiChar;
  } Char;
  WORD  Attributes;
} CHAR_INFO, *PCHAR_INFO;

typedef struct _COORD {
  SHORT X;
  SHORT Y;
} COORD, *PCOORD;

typedef struct _SMALL_RECT {
  SHORT Left;
  SHORT Top;
  SHORT Right;
  SHORT Bottom;
} SMALL_RECT;


typedef struct _CONSOLE_SCREEN_BUFFER_INFO {
  COORD dwSize;
  COORD dwCursorPosition;
  WORD wAttributes;
  SMALL_RECT srWindow;
  COORD dwMaximumWindowSize;
} CONSOLE_SCREEN_BUFFER_INFO;

typedef struct _SECURITY_ATTRIBUTES {
  DWORD nLength;
  LPVOID lpSecurityDescriptor;
  BOOL bInheritHandle;
} SECURITY_ATTRIBUTES, *PSECURITY_ATTRIBUTES, *LPSECURITY_ATTRIBUTES;

typedef struct _TEST_STRUCT_A {
    int a;
    byte b; 
    byte c;
    byte d;
} TEST_STRUCT_A;

typedef struct _TEST_STRUCT_B {
    byte a;
    int b;
    byte c;
} TEST_STRUCT_B;

typedef struct _TEST_STRUCT_C {
    byte a;
    int b;
    long c;
} TEST_STRUCT_C;


typedef struct _TEST_STRUCT_D {
    TEST_STRUCT_A a;
    TEST_STRUCT_B b;
    TEST_STRUCT_C c;
} TEST_STRUCT_D;

typedef struct _TEST_STRUCT_A10 {
    SHORT a;
    SHORT b;
    SHORT c;
    SHORT d;
    SHORT e;
} TEST_STRUCT_A10;

typedef struct _TEST_STRUCT_D2 {
    TEST_STRUCT_A10 a;
    TEST_STRUCT_B b;
    TEST_STRUCT_C c;
} TEST_STRUCT_D2;

int GetStdHandle(int handleType);

int GetConsoleScreenBufferInfo(void *hConsoleOutput, CONSOLE_SCREEN_BUFFER_INFO *screenBufferInfo);

int WriteConsoleOutput(void *hConsoleOutput, const CHAR_INFO *lpBuffer, COORD dwBufferSize, COORD dwBufferCoord, SMALL_RECT *lpWriteRegion);

int ScrollConsoleScreenBuffer(void *hConsoleOutput, const SMALL_RECT *lpsr, const SMALL_RECT *lpcr, COORD dstCoord, const CHAR_INFO *lpfc);

int SetConsoleScreenBufferSize(void *hConsoleOutput, COORD dwSize);

int CreateConsoleScreenBuffer(DWORD dwDesiredAccess, DWORD dwShareMode, const SECURITY_ATTRIBUTES *lpSecurityAttributes, DWORD dwFlags, LPVOID lpScreenBufferData);

int testReadWriteCharInfoArray(CHAR_INFO *charInfoArray, COORD arraySize);

void printSize();

#endif

#ifdef __cplusplus
} // __cplusplus defined.
#endif