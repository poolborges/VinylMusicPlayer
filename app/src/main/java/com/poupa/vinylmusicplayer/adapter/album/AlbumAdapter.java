package com.poupa.vinylmusicplayer.adapter.album;

import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.adapter.base.AbsMultiSelectAdapter;
import com.poupa.vinylmusicplayer.adapter.base.MediaEntryViewHolder;
import com.poupa.vinylmusicplayer.glide.GlideApp;
import com.poupa.vinylmusicplayer.glide.VinylColoredTarget;
import com.poupa.vinylmusicplayer.glide.VinylGlideExtension;
import com.poupa.vinylmusicplayer.helper.SortOrder;
import com.poupa.vinylmusicplayer.helper.menu.SongsMenuHelper;
import com.poupa.vinylmusicplayer.interfaces.CabHolder;
import com.poupa.vinylmusicplayer.model.Album;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.MusicUtil;
import com.poupa.vinylmusicplayer.util.NavigationUtil;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumAdapter extends AbsMultiSelectAdapter<AlbumAdapter.ViewHolder, Album> implements FastScrollRecyclerView.SectionedAdapter {

    protected final AppCompatActivity activity;
    protected ArrayList<Album> dataSet;

    protected int itemLayoutRes;

    protected boolean usePalette = false;

    public AlbumAdapter(@NonNull AppCompatActivity activity, ArrayList<Album> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
        this.usePalette = usePalette;

        setHasStableIds(true);
    }

    public void usePalette(boolean usePalette) {
        this.usePalette = usePalette;
        notifyDataSetChanged();
    }

    public void swapDataSet(ArrayList<Album> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    public ArrayList<Album> getDataSet() {
        return dataSet;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false);
        return createViewHolder(view, viewType);
    }

    protected ViewHolder createViewHolder(View view, int viewType) {
        return new ViewHolder(view);
    }

    protected String getAlbumTitle(Album album) {
        return album.getTitle();
    }

    protected String getAlbumText(Album album) {
        return album.getArtistName();
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Album album = dataSet.get(position);

        final boolean isChecked = isChecked(album);
        holder.itemView.setActivated(isChecked);

        if (holder.getAdapterPosition() == getItemCount() - 1) {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.GONE);
            }
        } else {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.VISIBLE);
            }
        }

        if (holder.title != null) {
            holder.title.setText(getAlbumTitle(album));
        }
        if (holder.text != null) {
            holder.text.setText(getAlbumText(album));
        }

        loadAlbumCover(album, holder);
    }

    protected void setColors(int color, ViewHolder holder) {
        if (holder.paletteColorContainer != null) {
            holder.paletteColorContainer.setBackgroundColor(color);
            if (holder.title != null) {
                holder.title.setTextColor(MaterialValueHelper.getPrimaryTextColor(activity, ColorUtil.isColorLight(color)));
            }
            if (holder.text != null) {
                holder.text.setTextColor(MaterialValueHelper.getSecondaryTextColor(activity, ColorUtil.isColorLight(color)));
            }
        }
    }

    protected void loadAlbumCover(Album album, final ViewHolder holder) {
        if (holder.image == null) return;

        GlideApp.with(activity)
                .asBitmapPalette()
                .load(VinylGlideExtension.getSongModel(album.safeGetFirstSong()))
                .transition(VinylGlideExtension.getDefaultTransition())
                .songOptions(album.safeGetFirstSong())
                .into(new VinylColoredTarget(holder.image) {
                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
                        setColors(getDefaultFooterColor(), holder);
                    }

                    @Override
                    public void onColorReady(int color) {
                        if (usePalette)
                            setColors(color, holder);
                        else
                            setColors(getDefaultFooterColor(), holder);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).getId();
    }

    @Override
    protected Album getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected String getName(Album album) {
        return album.getTitle();
    }

    @Override
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull ArrayList<Album> selection) {
        SongsMenuHelper.handleMenuClick(activity, getSongList(selection), menuItem.getItemId());
    }

    @NonNull
    private ArrayList<Song> getSongList(@NonNull List<Album> albums) {
        final ArrayList<Song> songs = new ArrayList<>();
        for (Album album : albums) {
            songs.addAll(album.songs);
        }
        return songs;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        @Nullable String sectionName = null;
        switch (PreferenceUtil.getInstance().getAlbumSortOrder()) {
            case SortOrder.AlbumSortOrder.ALBUM_A_Z:
            case SortOrder.AlbumSortOrder.ALBUM_Z_A:
                sectionName = dataSet.get(position).getTitle();
                break;
            case SortOrder.AlbumSortOrder.ALBUM_ARTIST:
                sectionName = dataSet.get(position).getArtistName();
                break;
            case SortOrder.AlbumSortOrder.ALBUM_YEAR:
                return MusicUtil.getYearString(dataSet.get(position).getYear());
        }

        return MusicUtil.getSectionName(sectionName);
    }

    public class ViewHolder extends MediaEntryViewHolder {

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            setImageTransitionName(activity.getString(R.string.transition_album_art));
            if (menu != null) {
                menu.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                Pair[] albumPairs = new Pair[]{
                        Pair.create(image,
                                activity.getResources().getString(R.string.transition_album_art)
                        )};
                NavigationUtil.goToAlbum(activity, dataSet.get(getAdapterPosition()).getId(), albumPairs);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
