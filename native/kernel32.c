
  
// Header file for input output functions
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>

#include "kernel32.h"

void appendLine(char *x) {
   FILE *out_file = fopen("out.log", "a");       
   fprintf(out_file, "%s", x);
   fclose(out_file);
}

void overlay(char *text, int *startIndexRef, const char *overlayText) {
    int otLen = strlen(overlayText);
    int startIndex = *(startIndexRef);
    for (int i=0; i<otLen; i++){
        text[startIndex+i] = overlayText[i];
    }
    text[startIndex+otLen] = '\0';
    *(startIndexRef) = startIndex + otLen;
}

void overlayCharInfo(char *text, int *startIndexRef, CHAR_INFO charInfo) {
    char charInfoVal[20] = {'(', charInfo.Char.AsciiChar,',','\0'};
    char attribVal[10]; 
    char suffix[2] = {')','\0'};
    int i=3;

    snprintf(attribVal, 10, "%d", charInfo.Attributes);
    overlay(charInfoVal, &i, attribVal);
    overlay(charInfoVal, &i, suffix);
    
    overlay(text, startIndexRef, charInfoVal);
}

void overlayCoord(char *text, int *startIndexRef, COORD coord) {
    char overlayText[20] = {'(', '\0'};
    char attribVal[10];
    char suffix[2] = {')', '\0'};
    int i=1;
    
    snprintf(attribVal, 10, "%d,", coord.X);
    overlay(overlayText, &i, attribVal);

    snprintf(attribVal, 10, "%d", coord.Y);
    overlay(overlayText, &i, attribVal);
    overlay(overlayText, &i, suffix);

    overlay(text, startIndexRef, overlayText);
}

void overlaySmallRect(char *text, int *startIndexRef, SMALL_RECT rect) {
    char overlayText[40] = {'(', '\0'};
    char attribVal[10];
    char suffix[2] = {')', '\0'};
    int i=1;

    snprintf(attribVal, 10, "%d,", rect.Left);
    overlay(overlayText, &i, attribVal);

    snprintf(attribVal, 10, "%d,", rect.Top);
    overlay(overlayText, &i, attribVal);

    snprintf(attribVal, 10, "%d,", rect.Right);
    overlay(overlayText, &i, attribVal);

    snprintf(attribVal, 10, "%d", rect.Bottom);
    overlay(overlayText, &i, attribVal);
    overlay(overlayText, &i, suffix);

    overlay(text, startIndexRef, overlayText);
}

int GetConsoleScreenBufferInfo(void *hConsoleOutput, CONSOLE_SCREEN_BUFFER_INFO *info) {
    
    char text[100];
    
    int i=0;

    overlay(text, &i, "GetConsoleScreenBufferInfo: srWindow=");
    overlaySmallRect(text, &i, info->srWindow);
    overlay(text, &i, "\n");

    appendLine(text);
 
    info->dwSize.X=9;
    info->dwSize.Y=5;

    info->srWindow.Left = 1;
    info->srWindow.Top = 3;
    info->srWindow.Right = 5;
    info->srWindow.Bottom = 7;

    return 0;   
}

int SetConsoleScreenBufferSize(void *hConsoleOutput, COORD dwSize) {
    char text[100];
    int i=0;
    overlay(text, &i, "SetConsoleScreenBufferSize: dwSize=");
    overlayCoord(text, &i, dwSize);
    overlay(text, &i, "\n");
    appendLine(text);
    return 0;
}

int ScrollConsoleScreenBuffer(void *hConsoleOutput, const SMALL_RECT *lpsr, const SMALL_RECT *lpcr, COORD dstCoord, const CHAR_INFO *lpfc){
    char text[120];
    int i=0;

    SMALL_RECT scrollRect = *(lpsr);
    
    overlay(text, &i, "ScrollConsoleScreenBuffer:");

    overlay(text, &i, "scrollRect=");
    overlaySmallRect(text, &i, scrollRect);
    
    overlay(text, &i, ", clipRect=");
    if (lpcr != NULL) {
        SMALL_RECT clipRect = *(lpcr);
        overlaySmallRect(text, &i, clipRect);
    } else {
    	overlay(text, &i, "NULL");
    } 

    overlay(text, &i, ", dstCoord=");
    overlayCoord(text, &i, dstCoord);
    
    CHAR_INFO fillChar = *(lpfc);

    overlay(text, &i, ", fillChar=");
    overlayCharInfo(text, &i, fillChar);
    overlay(text, &i, "\n");

    appendLine(text);
    return 0;
}



int WriteConsoleOutput(void *hConsoleOutput, const CHAR_INFO *lpBuffer, COORD dwBufferSize, COORD dwBufferCoord, SMALL_RECT *lpWriteRegion) {
    
    int lpBufferLen = (dwBufferSize.X * dwBufferSize.Y);
 
    SMALL_RECT lpwrVal = *(lpWriteRegion);

    int charArraySize = lpBufferLen * 10 + 30;
    char caVal[charArraySize];

    int i=0;

    overlay(caVal, &i, "WriteConsoleOutput: ");
    overlay(caVal, &i, "CharInfo=[");

    for (int b=0; b<lpBufferLen; b++){
        overlayCharInfo(caVal, &i, lpBuffer[b]);
    }

    overlay(caVal, &i, "]");

    overlay(caVal, &i, ",bufferSize=");
    overlayCoord(caVal, &i, dwBufferSize);

    overlay(caVal, &i, ",bufferCoord=");
    overlayCoord(caVal, &i, dwBufferCoord);

    overlay(caVal, &i, ",writeRegion=");
    overlaySmallRect(caVal, &i, lpwrVal);

    overlay(caVal, &i, "\n");
    
    appendLine(caVal);
    

    lpWriteRegion->Left = 1;
    lpWriteRegion->Top = 3;
    lpWriteRegion->Right = 5;
    lpWriteRegion->Bottom = 7;

    return 0; 
}

void testA(CONSOLE_SCREEN_BUFFER_INFO *ptrVal){
   ptrVal->srWindow.Left=0;
   ptrVal->srWindow.Top=2;
   ptrVal->srWindow.Right=4;
   ptrVal->srWindow.Bottom=6;
}

void test() {
   CONSOLE_SCREEN_BUFFER_INFO info;
   
   SMALL_RECT val= {.Left=1, .Top=3, .Right=5, .Bottom=7};
   info.srWindow = val;

   testA(&info);
   
   printf("new val: .Left=%d,.Top=%d,.Right=%d,.Bottom=%d", info.srWindow.Left, info.srWindow.Top, info.srWindow.Right, info.srWindow.Bottom);

}


void printArraySize(CHAR_INFO *array) {
   printf("sizeof(*array)=%lu", sizeof(*array));
}

void printSize() {
   printf("CHAR_INFO=%lu\n", sizeof(CHAR_INFO));
   printf("COORD=%lu\n", sizeof(COORD));
   printf("SMALL_RECT=%lu\n", sizeof(SMALL_RECT));
   printf("CONSOLE_SCREEN_BUFFER_INFO=%lu\n", sizeof(CONSOLE_SCREEN_BUFFER_INFO));
   printf("TEST_STRUCT_A=%lu\n", sizeof(TEST_STRUCT_A));
   printf("TEST_STRUCT_B=%lu\n", sizeof(TEST_STRUCT_B));
   printf("TEST_STRUCT_C=%lu\n", sizeof(TEST_STRUCT_C));
   printf("TEST_STRUCT_D=%lu\n", sizeof(TEST_STRUCT_D));
   printf("TEST_STRUCT_A10=%lu\n", sizeof(TEST_STRUCT_A10));
   printf("TEST_STRUCT_D2=%lu\n", sizeof(TEST_STRUCT_D2));
   
   CHAR_INFO array[3];
   printArraySize(array);

}

int CreateConsoleScreenBuffer(DWORD dwAccess, DWORD shareMode, const SECURITY_ATTRIBUTES *securityAttributes, DWORD dwShare, LPVOID lpBufferData) {
    char input[100];
    char val[10];

    int i=0;

    SECURITY_ATTRIBUTES attr = *(securityAttributes);

    overlay(input, &i, "CreateConsoleScreenBuffer:");

    overlay(input, &i, " dwAccess=");
    snprintf(val, 10, "%d", dwAccess);
    overlay(input, &i, val);

    overlay(input, &i, " shareMode=");
    snprintf(val, 10, "%d", shareMode);
    overlay(input, &i, val);

    overlay(input, &i, " securityAttribute=");
    snprintf(val, 10, "%d", attr.nLength);
    overlay(input, &i, val);

    overlay(input, &i, " dwShare=");
    snprintf(val, 10, "%d", dwShare);
    overlay(input, &i, val);

    appendLine(input);

    return 0;
}

int testReadWriteCharInfoArray(CHAR_INFO *charInfoArray, COORD arraySize) {
   SHORT n = arraySize.X * arraySize.Y;

   for(SHORT i=0; i<n; i++) {
      charInfoArray[i].Char.AsciiChar += 32;
      charInfoArray[i].Attributes += 32;
   }

   return 0;
}

int main(void)
{
    //printSize();
    FILE *stream;
    char mode[1];
    int feofResult, fgetcResult;

    mode[0] = 'r';
    stream = fdopen(0, mode);

    feofResult = feof(stream);
    fgetcResult = fgetc(stream);

    printf("feof=%d", feofResult);
    printf("fgetc=%d", fgetcResult);


    feofResult = feof(stream);
    printf("feof=%d", feofResult);

    return 0;
}

