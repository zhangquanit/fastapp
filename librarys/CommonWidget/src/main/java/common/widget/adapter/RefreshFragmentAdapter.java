package common.widget.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

/**
 * 支持刷新的FragmentPagerAdapter
 */
public class RefreshFragmentAdapter extends FragmentPagerAdapter {

    private List<String> mTitleList;

    private List<Fragment> mFragmentList;

    private FragmentManager fm;

    public RefreshFragmentAdapter(FragmentManager fm, List<String> titleList, List<Fragment> fragmentList) {

        super(fm);

        this.fm = fm;
        this.mTitleList = titleList;
        this.mFragmentList = fragmentList;

    }

    @Override

    public Fragment getItem(int position) {
        return mFragmentList.get(position);

    }

    @Override

    public int getCount() {
        return mFragmentList.size();

    }

    public void setFragments(List<String> titleList, List<Fragment> fragments) {

        if (this.mFragmentList != null) {

            FragmentTransaction ft = fm.beginTransaction();

            for (Fragment f : this.mFragmentList) {
                ft.remove(f);
            }

            ft.commit();
            fm.executePendingTransactions();
        }

        this.mTitleList = titleList;
        this.mFragmentList = fragments;

        notifyDataSetChanged();

    }

    @Override

    public int getItemPosition(Object object) {

        return POSITION_NONE;

    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitleList.get(position);
    }

}