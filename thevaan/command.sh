#!/bin/sh

revision="command v0.1.0 (2022-06-15 by MIBonk & MIB-Wiki)"
# use --help for more info
# Copy app.img on file level

export PATH=:/proc/boot:/sbin:/bin:/usr/bin:/usr/sbin:/net/mmx/bin:/net/mmx/usr/bin:/net/mmx/usr/sbin:/net/mmx/sbin:/net/mmx/mnt/app/armle/bin:/net/mmx/mnt/app/armle/sbin:/net/mmx/mnt/app/armle/usr/bin:/net/mmx/mnt/app/armle/usr/sbin
export LD_LIBRARY_PATH=/net/mmx/mnt/app/root/lib-target:/net/mmx/mnt/eso/lib:/net/mmx/eso/lib:/net/mmx/mnt/app/usr/lib:/net/mmx/mnt/app/armle/lib:/net/mmx/mnt/app/armle/lib/dll:/net/mmx/mnt/app/armle/usr/lib
export IPL_CONFIG_DIR=/etc/eso/production

mount -uw /net/mmx/fs/sda0/
mount -uw /net/mmx/mnt/app/


if [[ "$TRAINVERSION" = *VWG1* ]] || [[ "$TRAINVERSION" = *SKG1* ]] || [[ "$TRAINVERSION" = *SEG1* ]]; then
  cp /net/mmx/fs/sda0/NavActiveIgnore-VW.jar /net/mmx/mnt/app/eso/hmi/lsd/jars/NavActiveIgnore.jar
else
  cp /net/mmx/fs/sda0/NavActiveIgnore-AU.jar /net/mmx/mnt/app/eso/hmi/lsd/jars/NavActiveIgnore.jar
fi

exit 0
