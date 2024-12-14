package io.github.abhishekabhi789.mdnshelper.nsd

/** The API used to discover nearby services*/
enum class DiscoverMethod {
    NsdManager, RxDnsSd;

    /** Returns the [ResolvingMethod] methods for a [DiscoverMethod]*/
    fun getSupportedResolvingMethod(): List<ResolvingMethod> {
        return when (this) {
            NsdManager -> listOf(ResolvingMethod.NsdManager, ResolvingMethod.RxDnsSd)
            RxDnsSd -> listOf(ResolvingMethod.RxDnsSd)
        }
    }
}

/** The API used to resolve infos of a service*/
enum class ResolvingMethod {
    NsdManager, RxDnsSd
}
