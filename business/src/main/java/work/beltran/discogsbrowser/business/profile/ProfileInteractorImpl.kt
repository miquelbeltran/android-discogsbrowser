package work.beltran.discogsbrowser.business.profile

import io.reactivex.Single
import work.beltran.discogsbrowser.api.DiscogsService
import work.beltran.discogsbrowser.api.model.UserIdentity
import work.beltran.discogsbrowser.api.model.UserProfile
import work.beltran.discogsbrowser.business.ProfileInteractor
import work.beltran.discogsbrowser.business.RxJavaSchedulers

/**
 * Created by Miquel Beltran on 8/27/16
 * More on http://beltran.work
 */
class ProfileInteractorImpl(private val service: DiscogsService,
                            private val schedulers: RxJavaSchedulers)
    : ProfileInteractor {

    override val identity: Single<UserIdentity>
        get() = service.userIdentity
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.mainThread())

    override val profile: Single<UserProfile>
        get() = service.userIdentity
                .flatMap { service.getUserProfile(it.username) }
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.mainThread())
}
