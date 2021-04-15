package org.tasks.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.text.util.Linkify.*
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import org.tasks.R

object Context {
    private const val HTTP = "http"
    private const val HTTPS = "https"

    fun Context.safeStartActivity(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toast(R.string.no_app_found)
        }
    }

    fun Context.openUri(resId: Int, vararg formatArgs: Any) = openUri(getString(resId, formatArgs))

    fun Context.openUri(url: String?) =
        url?.let { Uri.parse(it) }?.let {
            when {
                it.scheme.equals(HTTPS, true) || it.scheme.equals(HTTP, true) ->
                    try {
                        CustomTabsIntent.Builder()
                            .setUrlBarHidingEnabled(true)
                            .setShowTitle(true)
                            .setShareState(CustomTabsIntent.SHARE_STATE_ON)
                            .build()
                            .launchUrl(this, it)
                    } catch (e: ActivityNotFoundException) {
                        toast(R.string.no_app_found)
                    }
                else -> safeStartActivity(Intent(ACTION_VIEW, it))
            }
        }

    fun Context.toast(resId: Int, vararg formatArgs: Any, duration: Int = Toast.LENGTH_LONG) =
        toast(getString(resId, *formatArgs), duration)

    fun Context.toast(text: String?, duration: Int = Toast.LENGTH_LONG) =
        text?.let { Toast.makeText(this, it, duration).show() }

    fun Context.markwon(linkify: Boolean = false): Markwon {
        val builder = Markwon
            .builder(this)
            .usePlugins(
                listOf(
                    TaskListPlugin.create(this),
                    TablePlugin.create(this),
                    StrikethroughPlugin.create()
                )
            )
        if (linkify) {
            builder.usePlugin(
                LinkifyPlugin.create(WEB_URLS or EMAIL_ADDRESSES or PHONE_NUMBERS, true)
            )
        }
        return builder.build()
    }
}