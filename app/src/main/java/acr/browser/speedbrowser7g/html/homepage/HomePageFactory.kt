package acr.browser.speedbrowser7g.html.homepage

import acr.browser.speedbrowser7g.R
import acr.browser.speedbrowser7g.constant.FILE
import acr.browser.speedbrowser7g.constant.UTF8
import acr.browser.speedbrowser7g.html.HtmlPageFactory
import acr.browser.speedbrowser7g.html.jsoup.*
import acr.browser.speedbrowser7g.search.SearchEngineProvider
import android.app.Application
import io.reactivex.Single
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

/**
 * A factory for the home page.
 */
class HomePageFactory @Inject constructor(
    private val application: Application,
    private val searchEngineProvider: SearchEngineProvider,
    private val homePageReader: HomePageReader
) : HtmlPageFactory {

    private val title = application.getString(R.string.home)

    override fun buildPage(): Single<String> = Single
        .just(searchEngineProvider.provideSearchEngine())
        .map { (iconUrl, queryUrl, _, feedsData) ->
            parse(homePageReader.provideHtml()) andBuild {
                title { title }
                charset { UTF8 }
                body {
                    //id("image_url") { attr("src", iconUrl) }
                    val repeatedElement = id("repeated").removeElement()
                    id("content") {
                        feedsData.forEach {
                            appendChild(repeatedElement.clone {
                                id("urlFeed") { attr("href", it.Url) }
                                id("urlFeed") { text(it.Title) }
                                id("websiteFeed") { text(it.Website) }
                                id("descriptionFeed") { html(it.Description) }
                            })
                        }
                    }
                    tag("script") {
                        html(
                            html()
                                .replace("\${BASE_URL}", queryUrl)
                                .replace("&", "\\u0026")
                        )
                    }
                }
            }
        }
        .map { content -> Pair(createHomePage(), content) }
        .doOnSuccess { (page, content) ->
            FileWriter(page, false).use {
                it.write(content)
            }
        }
        .map { (page, _) -> "$FILE$page" }

    /**
     * Create the home page file.
     */
    fun createHomePage() = File(application.filesDir, FILENAME)

    companion object {

        const val FILENAME = "homepage.html"

    }

}
