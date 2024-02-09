/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import javax.persistence.Entity;
import javax.persistence.*;

/**
 * Class representing an item in DSpace.
 * <P>
 * This class holds in memory the item Dublin Core metadata, the bundles in the
 * item, and the bitstreams in those bundles. When modifying the item, if you
 * modify the Dublin Core or the "in archive" flag, you must call
 * <code>update</code> for the changes to be written to the database.
 * Creating, adding or removing bundles or bitstreams has immediate effect in
 * the database.
 *
 * @author ashivnmajethiya

 */
@Entity
@Table(name = "subcategory")
public class SubCategory extends DSpaceObject{

    @Column(name = "subcategoryname")
    private String subcategoryname;
    @ManyToOne
    @JoinColumn(name = "category_uuid")
    private Category category;
    @Override
    public int getType() {
        return 0;
    }
    @Override
    public String getName() {
        return "SubCategory";
    }
    public String getSubcategoryname() {
        return subcategoryname;
    }
    public void setSubcategoryname(String subcategoryname) {
        this.subcategoryname = subcategoryname;
    }
    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }
}
