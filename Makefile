########################################
# makefile for pd

MAKEFILES := $(wildcard */Makefile)

.PHONY: all
all: droid.common $(addsuffix .each, $(MAKEFILES))

.PHONY: droid.common
droid.common:
	@make -C droid

%/Makefile.each: %/Makefile
	@make -C $(dir $<)

.PHONY: install
install: $(addsuffix .install, $(MAKEFILES))

%/Makefile.install: %/Makefile
	@make -C $(dir $<) install

.PHONY: clean
clean: $(addsuffix .clean, $(MAKEFILES))

%/Makefile.clean: %/Makefile
	@make -C $(dir $<) clean
