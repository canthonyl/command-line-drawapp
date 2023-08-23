cc = gcc
ifeq ($(OS), Windows_NT)
	os_name = Win32
else
	os_name = $(shell uname -s 2>/dev/null || echo Unknown)
endif

ifeq ($(os_name), Win32)
	target = kernel32test.dll
	sys = $(shell $(cc) -dumpmachine)
	ifeq ($(sys), mingw32)
		cc = x86_64-w64-mingw32-gcc
	endif
	target_options = -s -Wl,--subsystem,windows
endif

ifeq ($(os_name), Darwin)
	target = libkernel32.dylib
endif

ifeq ($(os_name), Linux)
	target = libkernel32.so
endif

objects = $(target) kernel32.o

$(target): kernel32.o
	$(cc) -dynlib -shared $(target_options) -o $(target) kernel32.o

kernel32.o: kernel32.c kernel32.h
	$(cc) -std=c99 -c kernel32.c

clean:
	rm -f $(objects)