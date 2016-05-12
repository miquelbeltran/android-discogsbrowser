package work.beltran.discogsbrowser.ui.main.search;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import work.beltran.discogsbrowser.R;
import work.beltran.discogsbrowser.api.model.record.Record;
import work.beltran.discogsbrowser.api.network.AveragePrice;
import work.beltran.discogsbrowser.api.network.SearchSubject;
import work.beltran.discogsbrowser.databinding.CardRecordBinding;
import work.beltran.discogsbrowser.ui.errors.ErrorPresenter;
import work.beltran.discogsbrowser.ui.settings.Settings;

/**
 * Created by Miquel Beltran on 06.05.16.
 * More on http://beltran.work
 */
public class SearchRecordsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG = SearchRecordsAdapter.class.getCanonicalName();
    private List<Record> recordList = new ArrayList<>();
    private final Picasso picasso;
    private final SearchSubject subject;
    private ErrorPresenter errorPresenter;
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    private Settings settings;
    private AveragePrice averagePrice;
    private Subscription subscription;
    private boolean hasCompleted;

    public SearchRecordsAdapter(SearchSubject subject, Picasso picasso)  {
        this.picasso = picasso;
        this.subject = subject;
    }

    @Inject
    public void setErrorPresenter(ErrorPresenter errorPresenter) {
        this.errorPresenter = errorPresenter;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_ITEM:
            default:
                CardRecordBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.card_record, parent, false);
                return new RecordViewHolder(binding);
            case VIEW_PROG:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_item, parent, false);
                return new ProgressBarViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RecordViewHolder) {
            onBindViewHolder((RecordViewHolder) holder, position);
        }
    }

    protected void onBindViewHolder(final RecordViewHolder holder, int position) {
        holder.getBinding().setRecord(recordList.get(position));
        if (!recordList.get(position).getBasicInformation().getThumb().isEmpty()) {
            picasso.load(recordList.get(position).getBasicInformation().getThumb())
                    .tag(this)
                    .placeholder(R.drawable.music_record)
                    .fit()
                    .centerCrop()
                    .into(holder.getBinding().recordThumb);
        }
        boolean showPrices = settings.getSharedPreferences().getBoolean(getPreferencePrices(), getPreferencePricesDefault());
        if (showPrices) {
            String type = settings.getSharedPreferences().getString(getPreferencePricesType(), "0");
            Subscription subscription = averagePrice.getAveragePrice(recordList.get(position),
                    NumberFormat.getCurrencyInstance().getCurrency().getCurrencyCode(),
                    type)
                    .subscribe(new Observer<Double>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "onError: " + e.getMessage());
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(Double aDouble) {
                            NumberFormat format = NumberFormat.getCurrencyInstance();
                            holder.getBinding().textPrice.setText(format.format(aDouble));
                        }
                    });
            holder.setPriceSubscription(subscription);
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof RecordViewHolder) {
            onViewRecycled((RecordViewHolder) holder);
        }
    }

    private void onViewRecycled(RecordViewHolder holder) {
        holder.getBinding().textPrice.setText("");
        Subscription subscription = holder.getPriceSubscription();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    @Inject
    public void setAveragePrice(AveragePrice averagePrice) {
        this.averagePrice = averagePrice;
    }

    @Inject
    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public class RecordViewHolder extends RecyclerView.ViewHolder {
        private CardRecordBinding binding;
        private Subscription priceSubscription;

        public RecordViewHolder(CardRecordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public CardRecordBinding getBinding() {
            return binding;
        }

        public void setPriceSubscription(Subscription priceSubscription) {
            this.priceSubscription = priceSubscription;
        }

        public Subscription getPriceSubscription() {
            return priceSubscription;
        }
    }

    public class ProgressBarViewHolder extends RecyclerView.ViewHolder {

        public ProgressBarViewHolder(View view) {
            super(view);
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (showProgressbar() && position == recordList.size())
            return VIEW_PROG;
        else
            return VIEW_ITEM;
    }

    private boolean showProgressbar() {
        return subscription != null && !subscription.isUnsubscribed() && !hasCompleted;
    }

    @Override
    public int getItemCount() {
        return recordList.size() + (showProgressbar() ? 1 : 0);
    }

    protected void subscribe(Observable<Record> observable) {
        hasCompleted = false;
        subscription = observable
                .subscribe(new Observer<Record>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted()");
                        hasCompleted = true;
                        notifyItemRemoved(recordList.size());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError() " + e.getMessage());
                        errorPresenter.onError(e);
                    }

                    @Override
                    public void onNext(Record record) {
                        hasCompleted = false;
                        Log.d(TAG, "onNext(" + record.getInstance_id() + ")");
                        recordList.add(record);
                        notifyItemInserted(recordList.size() - 1);
                    }
                });
    }

    public void activityOnDestroy() {
        subscription.unsubscribe();
    }

    protected boolean getPreferencePricesDefault() {
        return false;
    }

    protected String getPreferencePricesType() {
        return Settings.COLLECTION_PRICES_TYPE;
    }

    protected String getPreferencePrices() {
        return Settings.COLLECTION_PRICES;
    }

    public void search(String query) {
        // remove all content
        recordList.clear();
        notifyDataSetChanged();
        // recreate subscription
        if (subscription != null)
            subscription.unsubscribe();
        subscribe(subject.search(query, 1));
        // show progressbar
        notifyDataSetChanged();
    }
}