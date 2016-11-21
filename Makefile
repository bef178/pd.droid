########################################
# makefile for pd

MAKEFILES := $(wildcard */Makefile)

.PHONY: all
all: droid $(addsuffix .each, $(MAKEFILES))

.PHONY: droid
droid:
	make -C droid

%/Makefile.each: %/Makefile
	make -C $(dir $<)

.PHONY: clean
clean: $(addsuffix .clean, $(MAKEFILES))

%/Makefile.clean: %/Makefile
	make -C $(dir $<) clean
