package io.horizontalsystems.bankwallet.modules.market.discovery

import io.horizontalsystems.bankwallet.core.Clearable
import io.horizontalsystems.bankwallet.core.IRateManager
import io.horizontalsystems.bankwallet.modules.market.top.Field
import io.horizontalsystems.bankwallet.modules.market.top.MarketCategory
import io.horizontalsystems.bankwallet.modules.market.top.MarketTopItem
import io.horizontalsystems.core.ICurrencyManager
import io.horizontalsystems.xrateskit.entities.Coin
import io.horizontalsystems.xrateskit.entities.CoinMarket
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject


class MarketDiscoveryService(
        private val marketCategoryProvider: MarketCategoryProvider,
        private val currencyManager: ICurrencyManager,
        private val xRateManager: IRateManager
) : Clearable {

    sealed class State {
        object Loading : State()
        object Loaded : State()
        data class Error(val error: Throwable) : State()
    }

    val sortingFields: Array<Field> = Field.values()

    val stateObservable: BehaviorSubject<State> = BehaviorSubject.createDefault(State.Loading)
    val currency by currencyManager::baseCurrency

    var marketTopItems: List<MarketTopItem> = listOf()
    var marketCategory: MarketCategory? = null
        set(value) {
            field = value
            fetch()
        }

    val marketCategories = listOf(
            MarketCategory.Rated, MarketCategory.Blockchains, MarketCategory.Privacy, MarketCategory.Scaling,
            MarketCategory.Infrastructure, MarketCategory.RiskManagement, MarketCategory.Oracles, MarketCategory.PredictionMarkets,
            MarketCategory.DefiAggregators, MarketCategory.Dexes, MarketCategory.Synthetics, MarketCategory.Metals,
            MarketCategory.Lending, MarketCategory.GamingAndVr, MarketCategory.FundRaising, MarketCategory.InternetOfThings,
            MarketCategory.B2B, MarketCategory.NFT, MarketCategory.Wallets, MarketCategory.Staking,
            MarketCategory.Stablecoins, MarketCategory.TokenizedBitcoin, MarketCategory.AlgoStablecoins, MarketCategory.ExchangeTokens
    )

    private var topItemsDisposable: Disposable? = null
    private val disposables = CompositeDisposable()

    init {
        fetch()
    }

    private fun fetch() {
        topItemsDisposable?.let { disposables.remove(it) }

        stateObservable.onNext(State.Loading)

        when (val category = marketCategory) {
            null -> {
                //get all coins
                xRateManager.getTopMarketList(currency.code)
                        .subscribeOn(Schedulers.io())
                        .subscribe({ list ->
                            marketTopItems = list.mapIndexed { index, topMarket ->
                                convertToMarketTopItem(index + 1, topMarket)
                            }
                            stateObservable.onNext(State.Loaded)
                        }, {
                            stateObservable.onNext(State.Error(it))
                        })
            }
            MarketCategory.Rated -> {
                //get coins with rating
//                marketCategoryProvider.getCoinRatingsAsync()
//                        .flatMap { coinRatingsMap ->
//                            val coins = coinRatingsMap.keys.map { Coin(it) }
//                            xRateManager.getCoinMarketList(coins, currency.code)
//
//                        }
//                        .subscribeOn(Schedulers.io())
//                        .subscribe({ coinRatingsMap ->
//
//                        }, {
//
//                        })
//                        .let {
//                            disposables.add(it)
//                        }
            }
            else -> {
                topItemsDisposable = marketCategoryProvider.getCoinCodesByCategoryAsync(category.id)
                        .flatMap { coinCodes ->
                            val coins = coinCodes.map { Coin(it) }
                            xRateManager.getCoinMarketList(coins, currency.code)
                        }
                        .subscribeOn(Schedulers.io())
                        .subscribe({ list ->
                            marketTopItems = list.mapIndexed { index, topMarket ->
                                convertToMarketTopItem(index + 1, topMarket)
                            }
                            stateObservable.onNext(State.Loaded)
                        }, {
                            stateObservable.onNext(State.Error(it))
                        })

                topItemsDisposable?.let {
                    disposables.add(it)
                }
            }
        }
    }

    private fun convertToMarketTopItem(rank: Int, topMarket: CoinMarket) =
            MarketTopItem(
                    rank,
                    topMarket.coin.code,
                    topMarket.coin.title,
                    topMarket.marketInfo.volume,
                    topMarket.marketInfo.rate,
                    topMarket.marketInfo.rateDiffPeriod,
                    topMarket.marketInfo.marketCap
            )

    override fun clear() {
        //TODO("not implemented")
    }

    fun refresh() {
        fetch()
    }


}
