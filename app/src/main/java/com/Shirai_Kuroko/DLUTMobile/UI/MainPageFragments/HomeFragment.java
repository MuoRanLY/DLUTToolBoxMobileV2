package com.Shirai_Kuroko.DLUTMobile.UI.MainPageFragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Shirai_Kuroko.DLUTMobile.Entities.GridAppID;
import com.Shirai_Kuroko.DLUTMobile.Helpers.ConfigHelper;
import com.Shirai_Kuroko.DLUTMobile.R;
import com.Shirai_Kuroko.DLUTMobile.UI.InnerBrowsers.BrowserActivity;
import com.Shirai_Kuroko.DLUTMobile.UI.OpenVirtualCardActivity;
import com.Shirai_Kuroko.DLUTMobile.UI.ScanQRCodeActivity;
import com.Shirai_Kuroko.DLUTMobile.UI.SearchActivity;
import com.Shirai_Kuroko.DLUTMobile.UI.ServiceManagement.AppGridManageActivity;
import com.Shirai_Kuroko.DLUTMobile.Utils.MobileUtils;
import com.bumptech.glide.Glide;
import com.google.zxing.integration.android.IntentIntegrator;
import com.youth.banner.Banner;

import java.util.List;

public class HomeFragment extends Fragment {

    MainGridAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @SuppressWarnings("ALL")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GridView mgv = requireView().findViewById(R.id.main_grid);
        List<GridAppID> gridAppIDS = ConfigHelper.GetGridIDList(requireContext());
        gridAppIDS.add(new GridAppID(-1));
        adapter = new MainGridAdapter(gridAppIDS);
        if (mgv != null) {
            mgv.setAdapter(adapter);
        } else {
            Toast.makeText(requireActivity(), "??????????????????????????????GridView", Toast.LENGTH_SHORT).show();
        }

        Banner banner = requireView().findViewById(R.id.banner);
        if (banner != null) {
            MobileUtils.GetGalllery(requireActivity(), banner);
        }

        ImageView imageView = requireView().findViewById(R.id.iv_search);
        imageView.setOnClickListener(view1 -> {
            Intent intent = new Intent(requireActivity(), SearchActivity.class);
            startActivity(intent);
        });
        ImageView iv_more = requireView().findViewById(R.id.iv_more);
        iv_more.setOnClickListener(this::showPopupWindow);
        RecyclerView CollectionCard = requireView().findViewById(R.id.CollectionCard);
        CollectionCard.setLayoutManager(new LinearLayoutManager(requireActivity()));

    }

    public void showPopupWindow(View view) {
        @SuppressLint("InflateParams")
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.popup_main_right_more, null);
        PopupWindow window = new PopupWindow(v, 360, 300, true);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setOutsideTouchable(true);
        window.setTouchable(true);
        window.setAnimationStyle(R.style.main_more_anim);
        window.showAsDropDown(view, 0, 0);
        v.findViewById(R.id.btn_start_qr).setOnClickListener(v1 -> {
            new IntentIntegrator(getActivity())
                    // ?????????Activity??????????????????----------------------------
                    .setCaptureActivity(ScanQRCodeActivity.class)
                    .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)// ???????????????,????????????????????????????????????/?????????
                    .setPrompt("????????????/???????????????????????????????????????")// ???????????????
                    .setCameraId(0)// ???????????????,???????????????????????????
                    .setBeepEnabled(false)// ??????????????????,??????????????????"???"?????????
                    .initiateScan();
            window.dismiss(); //??????????????????
        });
        v.findViewById(R.id.btn_start_virtual_card).setOnClickListener(v12 -> {
            Intent intent = new Intent(requireContext(), OpenVirtualCardActivity.class);
            startActivity(intent);
            window.dismiss();
        });
    }



    public class MainGridAdapter extends BaseAdapter {
        private List<GridAppID> mList;

        public MainGridAdapter(List<GridAppID> List) {
            this.mList = List;
        }

        public void showallgriditems(List<GridAppID> List) {
            mList = List;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint({"InflateParams", "SetTextI18n"})
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GridItemView gridItemView;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.main_grid_item, null);
                // ????????????????????????GridItemView????????????????????????
                gridItemView = new GridItemView();
                gridItemView.Icon = convertView.findViewById(R.id.main_grid_icon);
                gridItemView.Name = convertView.findViewById(R.id.main_grid_name);
                // ???GridItemView???????????????convertView
                convertView.setTag(gridItemView);
            } else {
                // ???convertView?????????ListItemView??????
                gridItemView = (GridItemView) convertView.getTag();
            }

            // ?????????mList??????????????????????????????
            int id = mList.get(position).getId();
            if (id != -1) {
                convertView.setOnClickListener(v -> {
                    Intent intent = new Intent(requireContext(), BrowserActivity.class);
                    intent.putExtra("App_ID", String.valueOf(id));
                    startActivity(intent);
                });
                // ??????????????????GridItemView??????????????????
                Glide.with(requireActivity()).load(ConfigHelper.Getmlist(requireContext()).get(id).getIcon()).into(gridItemView.Icon);
                gridItemView.Name.setText(ConfigHelper.Getmlist(requireContext()).get(id).getAppName());
            } else {
                convertView.setOnClickListener(v -> {
                    Intent intent = new Intent(requireContext(), AppGridManageActivity.class);
                    startActivity(intent);
                });
                Glide.with(requireActivity()).load(R.drawable.app_center_pre).into(gridItemView.Icon);
                gridItemView.Name.setText("????????????");
            }
            return convertView;
        }
    }

    static class GridItemView {
        ImageView Icon;
        TextView Name;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            List<GridAppID> gridAppIDS = ConfigHelper.GetGridIDList(requireContext());
            gridAppIDS.add(new GridAppID(-1));
            adapter.showallgriditems(gridAppIDS);
        }
    }
}