#!/bin/bash

function get_lib_out_pkg() {
    local mf=$1
    if test -n "$mf"; then
        if test -d "$mf"; then
            mf=$mf/AndroidManifest.xml
        fi
        if test -f "$mf"; then
            grep -oe 'package="\(.*\)"' $mf | cut -d \" -f2
            return
        fi
    fi
    >&2 echo "E: fail to find manifest [$mf]"
    return 1
}

function get_lib_out_jar() {
    local p=$1
    local pkg=$2
    echo $p/out/$pkg.jar
}

function get_lib_out_res() {
    local p=$1
    echo $p/out/res
}

####

opts=$(getopt -o m: --long meta: -- "$@")
if test $? != 0 ; then
    exit 1
fi
eval set -- "$opts"
while true ; do
    case "$1" in
        -m|--meta)
            meta=$2
            shift
            shift
            ;;
        --)
            shift
            break
            ;;
    esac
done
unset opts

case "$meta" in
    pkg|package)
        for p in $@; do
            get_lib_out_pkg $p
        done
        ;;
    jar)
        for p in $@; do
            pkg=$(get_lib_out_pkg $p)
            if test $? = 0; then
                get_lib_out_jar $p $pkg
            fi
        done
        ;;
    res)
        for p in $@; do
            # as check
            pkg=$(get_lib_out_pkg $p)
            if test $? = 0; then
                get_lib_out_res $p
            fi
        done
        ;;
    *)
        >&2 echo "E: unrecognized meta \"$meta\""
        ;;
esac
