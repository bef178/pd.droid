########################################
# makefile for pd

MAKEFILES := $(wildcard */Makefile)

.PHONY: each
all-each: $(addsuffix .each, $(MAKEFILES))

%/Makefile.each: %/Makefile
	make -C $(dir $<)

.PHONY: clean
clean: $(addsuffix .clean, $(MAKEFILES))

%/Makefile.clean: %/Makefile
	make -C $(dir $<) clean
