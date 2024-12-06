package in.tf.nira.manual.verification.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import in.tf.nira.manual.verification.constant.ErrorCode;
import in.tf.nira.manual.verification.dto.SearchSort;
import in.tf.nira.manual.verification.exception.RequestException;



/**
 * {@link SortUtils} use to sort the list based on the sort criteria this class
 * support multiple field sorting
 * 
 */
public class SortUtils {

	/**
	 * Method to sort the list based on the sorting parameter support mutliple sort
	 * criteria
	 * 
	 * @param <T>          list generic type
	 * @param list         input to be sorted
	 * @param sortCriteria sorting criteria
	 * @return sorted list
	 */
	public <T> List<T> sort(List<T> list, List<SearchSort> sortCriteria) {
		List<Field> fields = null;
		if (toBeSorted(list, sortCriteria)) {
			T data = list.get(0);
			fields = extractField(data);
			List<FieldComparator<T>> comparatorlist = new ArrayList<>();
			for (int i = 0; i < sortCriteria.size(); i++) {
				SearchSort sort = sortCriteria.get(i);
				comparatorlist.add(new FieldComparator<T>(findField(fields, sort.getSortField()), sort));
			}
			return list.parallelStream().sorted(new MultiFieldComparator<T>(comparatorlist))
					.collect(Collectors.toList());
		}
		return list;
	}

	/**
	 * Method to verify sorting criteria and the list to be sorted are present
	 * 
	 * @param <T>          generic list type
	 * @param list         input list
	 * @param sortCriteria sort criteria input
	 * @return true if need sorting, false otherwise
	 */
	private <T> boolean toBeSorted(List<T> list, List<SearchSort> sortCriteria) {
		return (list != null && !list.isEmpty() && sortCriteria != null && !sortCriteria.isEmpty());
	}

	/**
	 * Method to extract the fields
	 * 
	 * @param <T>   generic type
	 * @param clazz input class
	 * @return {@link List} of {@link Field} for the input along with super class
	 *         {@link Field}
	 */
	private <T> List<Field> extractField(T clazz) {
		List<Field> fields = new ArrayList<>();
		fields.addAll(Arrays.asList(clazz.getClass().getDeclaredFields()));
		if (clazz.getClass().getSuperclass() != Object.class) {
			fields.addAll(Arrays.asList(clazz.getClass().getSuperclass().getDeclaredFields()));
		}
		return fields;
	}

	private Field findField(List<Field> fields, String name) {
		Optional<Field> field = fields.stream().filter(f -> f.getName().equalsIgnoreCase(name)).findFirst();
		if (StringUtils.isBlank(name)) {
			throw new RequestException(ErrorCode.INVALID_SORT_INPUT.getErrorCode(),
					ErrorCode.INVALID_SORT_INPUT.getErrorMessage());
		}
		if (field.isPresent()) {
			return field.get();
		} else {
			throw new RequestException(ErrorCode.INVALID_SORT_FIELD.getErrorCode(),
					String.format(ErrorCode.INVALID_SORT_FIELD.getErrorMessage(), name));

		}
	}
}
