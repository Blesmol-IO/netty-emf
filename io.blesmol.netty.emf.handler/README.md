# Netty EMF Handlers

* Save an `EObject` to some other `EObject` dynamically (currently inbound only)
* Decodes a `ByteBuf` to an `EObject` (currently an `EByteBufHolder`)

## TODO

* Abstract out the [HttpContentDecoder](https://netty.io/4.1/api/io/netty/handler/codec/http/HttpContentDecoder.html)-inspired `EmbeddedChannel` usage to decode to other `EObject` types