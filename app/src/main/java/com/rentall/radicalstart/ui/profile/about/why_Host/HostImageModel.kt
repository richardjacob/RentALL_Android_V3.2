package com.rentall.radicalstart.ui.profile.about.why_Host

class ImageModel(var image_drawable: Int = 0, var text_string: Int = 0) {

    fun getImage_drawables(): Int {
        return image_drawable
    }

    fun setImage_drawables(image_drawable: Int) {
        this.image_drawable = image_drawable
    }

    fun getText_srings(): Int {
        return text_string
    }

    fun setText_srings(text_string: Int) {
        this.text_string = text_string
    }


}