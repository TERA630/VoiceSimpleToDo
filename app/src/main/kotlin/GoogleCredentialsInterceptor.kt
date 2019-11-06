package com.example.voicesimpletodo

import com.google.auth.Credentials
import io.grpc.*
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

class GoogleCredentialsInterceptor(
    private val mCredentials: Credentials ) : ClientInterceptor {
    //　ここではCredentialsを受け取り､InterceptCall(Channelから来るClientCallをまずキャッチするところ)を定義する｡

    private var mLastMetadata: Map<String, List<String>>? = null
    private lateinit var mCached: Metadata

    override fun <ReqT, RespT> interceptCall(
        method: MethodDescriptor<ReqT, RespT>, // remoteでの処理内容の名前とパラメータの記述
        callOptions: CallOptions, // the runtime option to be applied to this call
        next: Channel // the channel which is being intercepted
    ): ClientCall<ReqT, RespT> {
        val newClientCallDelegate = next.newCall(method, callOptions) // methodDescriptorに沿って､remoteでの動作を定義する｡Startをすればそれが開始される｡
        val clientCall =
            object : ClientInterceptors.CheckedForwardingClientCall<ReqT, RespT>(newClientCallDelegate) { // io.grpc.ClientCall→ io.grpc.ForwardingClientCall→io.grpc.ClientInterceptorsCheckedForwardingClientCall
                // ForwardingClientCallは例外をロジックからCall Listenerに伝播する｡ ClientCall.start(ClientCall.listener,Metadata)はmisuse以外では例外を出すべきではない｡
                // CheckedFordingClientCallのCheckedStartメソッドは例外をThrowしても良い｡
                override fun checkedStart(responseListener: Listener<RespT>?, headers: Metadata)  { // checkedStartのみが抽象メソッドなので実装する必要がある｡
                                                                // URIからMetaDataを読んで､MetaがNullでない､かつ変更があった場合に､toHeadersでmetadataを処理､
                                                                // mCashedを更新して､headersにMergeする｡
                    // 上位のClientCall.startを開始するのだろう｡
                    val cachedSaved: Metadata
                    val uri = serviceUri(next, method)  // Authenticateされているかどうか､URIが適切かどうかを検証｡
                    synchronized(this) {
                        val latestMetadata = getRequestMetadata(uri)
                        if (mLastMetadata == null || mLastMetadata != latestMetadata) {
                            mLastMetadata = latestMetadata
                            mCached = toHeaders(mLastMetadata!!)

                        }
                        cachedSaved = mCached
                    }
                    headers.merge(cachedSaved)
                    delegate().start(responseListener, headers) // 通常は最後にdelegate.startを呼ぶようにする｡　例外があるときは　delegate.start(ここのnewClientCallDelegate)を呼ぶ前にThrowする｡
                                                                // そうすると､CheckedForwardingClientCallで例外を受け取り､responseListenerに電波できる｡
                                                                // そうしないと､ClientCall.Listener.onCloseが何度も呼ばれてしまうと
                }
            }
        return clientCall
    }

    @Throws(StatusException::class)
    private fun <ReqT, RespT> serviceUri(channel: Channel, method: MethodDescriptor<ReqT, RespT>): URI {
        val authority = channel.authority()
        if (authority == null) {
            throw Status.UNAUTHENTICATED
                .withDescription("Channel has no authority")
                .asException()
        } else {
            val scheme = "https"
            val defaultPort = 443
            val path = "/${MethodDescriptor.extractFullServiceName(method.fullMethodName)}"
            try {
                val uri = URI(scheme, authority, path, null, null)
                if (uri.port == defaultPort) {
                    removePort(uri)
                }
                return uri
            } catch (e: URISyntaxException) {
                throw Status.UNAUTHENTICATED
                    .withDescription("Unable to construct service URI for auth")
                    .withCause(e).asException()
            }
            // The default port must not be present,Alternative ports should be present.
        }
    }

    @Throws(StatusException::class)
    fun removePort(uri: URI): URI {
        try {
            return URI(
                uri.scheme, uri.userInfo, uri.host, -1, uri.path, uri.query, uri.fragment
            )
        } catch (e: URISyntaxException) {
            throw Status.UNAUTHENTICATED
                .withDescription("Unable to construct service URI after removing port")
                .withCause(e).asException()
        }
    }

    @Throws(StatusException::class)
    fun getRequestMetadata(uri: URI): Map<String, List<String>> {
        try {
            return mCredentials.getRequestMetadata(uri)
        } catch (e: IOException) {
            throw Status.UNAUTHENTICATED.withCause(e).asException()
        }
    }

    fun toHeaders(metadata: Map<String, List<String>>): Metadata {
        val headers =  Metadata()
        for (key in metadata.keys) {
            val headerKey = Metadata.Key.of(
                key, Metadata.ASCII_STRING_MARSHALLER
            )
            val list = metadata[key] ?: emptyList()
            for (value in list) {
                headers.put(headerKey, value)
            }
        }
        return headers
    }
}