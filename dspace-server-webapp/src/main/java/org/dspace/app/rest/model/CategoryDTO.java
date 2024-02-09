package org.dspace.app.rest.model;

public class CategoryDTO {
    private String categoryname;
    private String categoryuuid;
    private String subcategoryname;
    private String subcategoryuuid;

    public String getCategoryname() {
        return categoryname;
    }

    public void setCategoryname(String categoryname) {
        this.categoryname = categoryname;
    }

    public String getCategoryuuid() {
        return categoryuuid;
    }

    public void setCategoryuuid(String categoryuuid) {
        this.categoryuuid = categoryuuid;
    }

    public String getSubcategoryname() {
        return subcategoryname;
    }

    public void setSubcategoryname(String subcategoryname) {
        this.subcategoryname = subcategoryname;
    }

    public String getSubcategoryuuid() {
        return subcategoryuuid;
    }

    public void setSubcategoryuuid(String subcategoryuuid) {
        this.subcategoryuuid = subcategoryuuid;
    }
}
