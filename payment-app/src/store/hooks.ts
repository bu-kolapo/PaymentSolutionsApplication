import { useDispatch, useSelector } from 'react-redux';
import type { AppDispatch, RootState } from './index';

// Type-safe dispatch hook
export const useAppDispatch = () => useDispatch<AppDispatch>();

// Type-safe selector hook
export const useAppSelector = <TSelected = unknown>(
    selector: (state: RootState) => TSelected
): TSelected => {
    return useSelector(selector);
};