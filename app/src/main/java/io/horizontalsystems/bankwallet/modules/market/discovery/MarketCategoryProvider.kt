package io.horizontalsystems.bankwallet.modules.market.discovery

import io.reactivex.Single

class MarketCategoryProvider {

    fun getCoinCodesByCategoryAsync(categoryId: String): Single<List<String>> {
        return Single.just(listOf("BTC", "ETH", "BCH"))
    }

    fun getCoinRatingsAsync(): Single<Map<String, String>> {
        return Single.just(mapOf())
    }

}
