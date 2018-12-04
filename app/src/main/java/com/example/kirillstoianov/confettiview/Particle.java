package com.example.kirillstoianov.confettiview;

import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;


public class Particle
{
    //--------------------------------------------------------
    //	ATTRIBUTES
    //--------------------------------------------------------
    // Position actuelle
    private float 	position_x;
    private float 	position_y;
    // Vitesse actuelle
    private float 	speed_x;
    private float 	speed_y;
    // DurŽe de vie restante en Ms
    private int		life;
    // Objet Drawable reprŽsentant visuellement la particule
    private PaintDrawable drawable;



    //--------------------------------------------------------
    //	CONSTRUCTOR
    //--------------------------------------------------------
    // params : position en X et en Y
    public Particle( float newPositionX, float newPositionY )
    {
        // On dŽfini la position initiale de la particule
        this.setPositionX(newPositionX);
        this.setPositionY(newPositionY);

        // On dŽfini la vitesse initiale de la particule
        this.speed_x = (int)( (Math.random()*200*(-1))+100 );	// entre -100 et +100
        this.speed_y = (int)( (Math.random()*200*(-1))+100 );

        // On dŽfini alŽatoirement la durŽe de vie de la particule ( entre 500 et 1500 Ms )
        this.life = (int)( (Math.random()*1000)+500 );

        // On crŽe le Drawable avec une couleur alŽatoire
        this.drawable = new PaintDrawable( Color.rgb((int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255)) );
    }





    //--------------------------------------------------------
    //	METHODS
    //--------------------------------------------------------
    // TODO   : ajouter le GAP en Ms en paramtre
    // TODO 2 : ajouter le vecteur gravitŽ en paramtre (change avec l'orientation du tŽlŽphone)
    // TMP    : 20ms de gap
    public void update()
    {
        if(this.life >= 20)
        {
            this.life = this.life - 20;
        }
        else
        {
            this.life = 0;
        }
        // MAJ de la vitesse en fonction de la gravitŽ
        //this.speed_x = this.speed_x + 0;
        this.speed_y = this.speed_y + 10; // gravitŽ
        // MAJ de la position en fonction de la vitesse
        this.position_x = this.position_x + ((this.speed_x*20)/1000);
        this.position_y = this.position_y + ((this.speed_y*20)/1000);
    }




    //-----------------------------------------------------------------------
    // GETTER / SETTER
    //-----------------------------------------------------------------------
    public void setPositionX(float position_x) {
        this.position_x = position_x;
    }
    public float getPositionX() {
        return position_x;
    }
    public void setPositionY(float position_y) {
        this.position_y = position_y;
    }
    public float getPositionY() {
        return position_y;
    }
    public int getLife() {
        return life;
    }
    public PaintDrawable getDrawable() {
        return drawable;
    }



}