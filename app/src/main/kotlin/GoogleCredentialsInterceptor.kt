package com.example.voicesimpletodo

import com.google.auth.Credentials
import io.grpc.*
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

class GoogleCredentialsInterceptor(
    private val mCredentials: Credentials ) : ClientInterceptor {
    // ClientInterceptor :　CallがChannel に届く前､　Stub間　の　動作を設定する｡

    private var mLastMetadata: Map<String, List<String>>? = null
    private lateinit var mCached: Metadata

    override fun <ReqT, RespT> interceptCall(
        //
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        val newClientCallDelegate = next.newCall(method, callOptions)
        val clientCall =
            object : ClientInterceptors.CheckedForwardingClientCall<ReqT, RespT>(newClientCallDelegate) {

                override fun checkedStart(responseListener: Listener<RespT>?, headers: Metadata) {
                    val cachedSaved: Metadata
                    val uri = serviceUri(next, method)
                    synchronized(this) {
                        val latestMetadata = getRequestMetadata(uri)
                        if (mLastMetadata == null || mLastMetadata != latestMetadata) {
                            mLastMetadata = latestMetadata
                            mCached = toHeaders(mLastMetadata!!)

                        }
                        cachedSaved = mCached
                    }
                    headers.merge(cachedSaved)
                    delegate().start(responseListener, headers)
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
        val headers: Metadata = Metadata()
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