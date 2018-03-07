package au.org.garvan.kccg.annotations.pipeline.engine.annotators.phenotype.util;

public class GeneralUtil {

	public static String removeBrackets(String label) {
		int index = label.indexOf("(");
		if (index == -1) {
			return label;
		}
		
		int index2 = label.indexOf(")");
		if (index == -1) {
			return label;
		}

		String newLabel = label.substring(0, index).trim() + " " + label.substring(index2).trim();
		return newLabel;
	}
	
	public static String stripURI(String uri) {
		int index;
		if (uri.contains("#")) {
			index = uri.lastIndexOf("#");
		} else {
			index = uri.lastIndexOf("/");
		}

		return index != -1 ? uri.substring(index + 1).trim() : uri;
	}

	public static String[] splitURI(String uri) {
		String[] result = new String[2];

		int index;
		if (uri.contains("#")) {
			index = uri.lastIndexOf("#");
		} else {
			index = uri.lastIndexOf("/");
		}

		result[0] = uri.substring(0, index);
		result[1] = uri.substring(index + 1);

		return result;
	}

}
