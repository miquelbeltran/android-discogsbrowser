package work.beltran.discogsbrowser.business.collection;

import rx.Observable;
import rx.Scheduler;
import work.beltran.discogsbrowser.api.DiscogsService;
import work.beltran.discogsbrowser.api.model.UserCollection;
import work.beltran.discogsbrowser.api.model.UserIdentity;
import work.beltran.discogsbrowser.business.base.RecordsApi;

/**
 * Created by Miquel Beltran on 04.05.16.
 * More on http://beltran.work
 */
public class CollectionRecordsApi extends RecordsApi<UserCollection> {
    public CollectionRecordsApi(DiscogsService service,
                                Observable<UserIdentity> userIdentityObservable,
                                Scheduler subscribeOnScheduler,
                                Scheduler observeOnScheduler) {
        super(service, userIdentityObservable, subscribeOnScheduler, observeOnScheduler);
    }

    @Override
    protected Observable<UserCollection> serviceCallToGetRecords(UserIdentity userIdentity, int nextPage) {
        return service.listRecords(userIdentity.getUsername(), nextPage);
    }
}