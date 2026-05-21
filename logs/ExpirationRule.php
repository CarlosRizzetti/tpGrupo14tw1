<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Carbon\Carbon;

class ExpirationRule extends Model
{
    use HasFactory;
    protected $fillable = ['product_id', 'location', 'duration_ minutes'];

    public function product(){
        return $this->belongsTo(Product::class);
    }

    public function calculateExpirationDate($product, $defrosting, $offsetMinutes, $productLocation){
        $elaborationTime  = now()->subMinutes($offsetMinutes);
        $expirationTime = $elaborationTime->copy()->addMinutes($this->duration_minutes);
        $defrostingTime = $elaborationTime->copy()->addMinutes($defrosting);
        $productName = $product->name;
        return compact('elaborationTime', 'expirationTime','defrostingTime', 'productName', 'productLocation');
    }


}
