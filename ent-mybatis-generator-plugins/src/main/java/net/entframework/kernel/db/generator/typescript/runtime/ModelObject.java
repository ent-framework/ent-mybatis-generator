package net.entframework.kernel.db.generator.typescript.runtime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderClassName = "Builder")
public class ModelObject {

	private String name;

	private String type;

	private String description;

	private String camelName;

	private String path;

	private String modelPackage;

	private boolean tenant;

}
