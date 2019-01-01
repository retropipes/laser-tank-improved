/*  LLDS: Arbitrary dimension arrays for Java programs
Licensed under Apache 2.0. See the LICENSE file for details.

All support is handled via the GitHub repository: https://github.com/wrldwzrd89/lib-java-low-level-data-storage
 */
package com.puttysoftware.lasertank.improved;

public class CloneableObject implements Cloneable {
    // Constructor
    public CloneableObject() {
	super();
    }

    // Method
    @Override
    public CloneableObject clone() throws CloneNotSupportedException {
	return (CloneableObject) super.clone();
    }
}
