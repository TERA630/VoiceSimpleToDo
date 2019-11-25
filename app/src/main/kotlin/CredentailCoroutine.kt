package com.example.voicesimpletodo

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Log
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.SpeechGrpc
import io.grpc.internal.DnsNameResolverProvider
import io.grpc.okhttp.OkHttpChannelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

private const val mTag = "accessToken"
private const val GOOGLE_HOST_NAME = "speech.googleapis.com"
private const val portOfGoogleAPI = 443

private const val PREF_ACCESS_TOKEN_EXPIRATION_TIME = "access_token_expiration_time"
private const val PREF_ACCESS_TOKEN_VALUE = "access_token_value"

suspend fun credentialToApi(appContext: Context,vModel:MainViewModel) : SpeechGrpc.SpeechStub? {
        val scopeOfGoogleAPI  = Collections.singletonList("https://www.googleapis.com/auth/cloud-platform")
        try{
            val tokenFromPref = getTokenFromPref(appContext)
            val token = if (tokenFromPref == null ){
                Log.i("credentialCoroutine","Token is from net.")
                val tokenFromRemote = withContext(Dispatchers.IO) {  fetchTokenFromCredentialKey(appContext,scopeOfGoogleAPI) }
                saveTokenToPref(tokenFromRemote,appContext)
                tokenFromRemote
            } else {
                Log.i("CredentialCoroutine","Token is from preference.")
                tokenFromPref
            }
            return tokenToApi(token,scopeOfGoogleAPI)
        } catch (e: Resources.NotFoundException){
            Log.e(mTag, "Fail to get credential file")
            return null
        } catch (e: IOException){
            Log.e(mTag, "Fail to obtain access token from InputStream or refresh at ${e.stackTrace}")
            return null
        } catch (e: Resources.NotFoundException){
            Log.e(mTag,"raw File not found.")
            return null
        }
}
//    val fetchAgain = Long.max(
//        token.expirationTime.time - System.currentTimeMillis() - ACCESS_TOKEN_FETCH_MARGIN,
//        ACCESS_TOKEN_EXPIRATION_TOLERANCE.toLong()

    private fun fetchTokenFromCredentialKey(appContext: Context,scope:MutableList<String>): AccessToken {
        val credentialIS = appContext.resources.openRawResource(R.raw.credential)
        val credentials = GoogleCredentials.fromStream(credentialIS).createScoped(scope)
        return credentials.refreshAccessToken()
    }
    private fun tokenToApi(token: AccessToken, scope: MutableList<String>):SpeechGrpc.SpeechStub{
        val googleCredentials = GoogleCredentials(token).createScoped(scope)
        val interceptor = GoogleCredentialsInterceptor(googleCredentials)
        val channel = OkHttpChannelProvider() // io.grpc.ManegedChannelProviderの派生クラス
            .builderForAddress(GOOGLE_HOST_NAME, portOfGoogleAPI) // hostとtargetURI(Address)を元にChannelを作る｡
            .nameResolverFactory(DnsNameResolverProvider())     // resolverFactoryを設定する｡
            .intercept(interceptor)                             // Channelが実際に呼ばれる前の前処置を設定する｡
            .build()
        return SpeechGrpc.newStub(channel)
    }
    private fun getTokenFromPref(appContext: Context): AccessToken? {
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE) ?: return null
        val tokenValue = prefs.getString(PREF_ACCESS_TOKEN_VALUE, null) ?: return null
        val expirationTime = prefs.getLong(PREF_ACCESS_TOKEN_EXPIRATION_TIME, -1L)

        val token = tokenValue.takeUnless { it.isEmpty() || expirationTime < 0 }
            .takeIf { expirationTime > System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TOLERANCE }
        return token?.let{ AccessToken( it , Date(expirationTime)) }
    }
    private fun saveTokenToPref(token: AccessToken,appContext: Context) {
        val prefs: SharedPreferences = appContext.getSharedPreferences(PREF_ACCESS_TOKEN_VALUE, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(PREF_ACCESS_TOKEN_VALUE, token.tokenValue)
            .putLong(PREF_ACCESS_TOKEN_EXPIRATION_TIME, token.expirationTime.time)
            .apply()
    }