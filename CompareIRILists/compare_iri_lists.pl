#!/usr/bin/perl

#####################################################################
#
# Purpose: Given the output from CompareOntology2BridgeFile 
# for NIF-Subcellular compared to different Bridge file, find which 
# equivalent class statements do not exist in the go-nifstd-bridge 
# file comparison.
#  
# Usage: 
#
# Author: Trish Whetzel (plwhetzel@gmail.com)
# Date: Thu Jul 17 10:35:32 PDT 2014
#######################################################################
use strict;
use warnings;
use Array::Utils;
use Data::Dumper;

open( NIFLIST, "< /Users/whetzel/git/CompareOntology2BridgeFile/CompareOntology2BridgeFile/NIF-GO-CC-Bridge.owl-noEquivClassList.txt" ) or die("Can't open input file: $!"); 
open( GOLIST, "< /Users/whetzel/git/CompareOntology2BridgeFile/CompareOntology2BridgeFile/go-nifstd-bridge-noEquivClassList.txt" ) or die("Can't open input file: $!"); 

open (NIFONLY, "> /Users/whetzel/Documents/workspace/CompareIRILists/result_files/unique2nifsao-bridgeDiff-file.txt") or die("Can't open output file: $!"); 
open (GOONLY, "> /Users/whetzel/Documents/workspace/CompareIRILists/result_files/unique2gosao-bridgeDiff-file.txt") or die("Can't open output file: $!"); 
open (COMMON1, "> /Users/whetzel/Documents/workspace/CompareIRILists/result_files/nif2go-common.txt") or die("Can't open output file: $!"); 
open (COMMON2, "> /Users/whetzel/Documents/workspace/CompareIRILists/result_files/go2nif-common.txt") or die("Can't open output file: $!"); 


#Declare variables


#check_unique_to_nif();
#check_unique_to_go();


	my @nif_lines = <NIFLIST>;
	my @go_lines = <GOLIST>;
	my (@new_go_lines, @new_nif_lines, $nif_line, @union, @intersection, @difference);
	
	#Format GO IRIs
	foreach (@go_lines) {
		$_ =~ s/^\s+|\s+$//g; #trim whitespace
		my $lt = "<";
		my $gt = ">";
		$_ =~ s/$lt//g;
		$_ =~ s/$gt//g;
		#print "GO Trimmed Line:\"$_\"\n";
		push (@new_go_lines, $_);
	}
	
	#Format NIF IRIs
	foreach (@nif_lines) {
		$_ =~ s/^\s+|\s+$//g; #trim whitespace
		my $old = "http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Subcellular.owl#";
		my $old1 = "http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Molecule.owl#";
		my $old2 = "http://ontology.neuinfo.org/NIF/Backend/BIRNLex-OBO-UBO.owl#";
		my $old3 = "http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-GrossAnatomy.owl#";
		my $old4 = "http://ontology.neuinfo.org/NIF/Backend/BIRNLex_annotation_properties.owl#";
		my $old5 = "http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Cell.owl#";
		
		my $new = "http://uri.neuinfo.org/nif/nifstd/";
		$_ =~ s/$old/$new/;  
		$_ =~ s/$old1/$new/;
		$_ =~ s/$old2/$new/;
		$_ =~ s/$old3/$new/;
		$_ =~ s/$old4/$new/; 
		$_ =~ s/$old5/$new/;
		
		my $lt = "<";
		my $gt = ">";
		$_ =~ s/$lt//g;
		$_ =~ s/$gt//g;
		#print "NIF Trimmed Line:\"$_\"\n";
		push (@new_nif_lines, $_);
	}
	
	
	
array_diff(\@new_go_lines, \@new_nif_lines);
	
sub array_diff {
    my @array1 = @{ shift() }; # GO
    my @array2 = @{ shift() }; # NIF 
    
    my $size1 = @array1; 
    my $size2 = @array2;
    
    print "ARRAY1: @array1\n";
    print "SIZE1: $size1\n\n";
    print "ARRAY2: @array2\n";
    print "SIZE1: $size2\n\n";
	
	my %array1_hash;
    my %array2_hash;

    # Create a hash entry for each element in @array1
    for my $element ( @array1 ) {
       $array1_hash{$element} = @array1;
    }
    
    # Same for @array2: This time, use map instead of a loop
    #map { $array_2{$_} = 1 } @array2; //error intializing
	for my $element ( @array2 ) {
       $array2_hash{$element} = @array2;
    }

	#Print Hashes - Debug
    #foreach(keys %array1_hash) { print "HASH1-CONTENT: $_ / $array1_hash{$_}\n"; }
	#print Dumper(\%array1_hash);
	#print Dumper(\%array2_hash);

	#From: http://docstore.mik.ua/orelly/perl/cookbook/ch05_12.htm, Find unique keys in two hashes
	# Find IRIs in GO list not in NIF list
	my @this_not_that = ();
		foreach (keys %array1_hash) {  #GO Hash values 
    	push(@this_not_that, $_) unless exists $array2_hash{$_};
	}
	my $tnt_size = @this_not_that;
	print "GOONLY-SIZE: $tnt_size\n";
	print "GOONLY-VALUES: @this_not_that\n";
	print GOONLY "$_\n" for @this_not_that;


	# Find IRIs in NIF list not in GO list
	my @nif_not_go = ();
	foreach (keys %array2_hash) {  # NIF Hash values
    	push(@nif_not_go, $_) unless exists $array1_hash{$_};
	}
	my $tng_size = @nif_not_go;
	print "NIFONLY-SIZE: $tng_size\n";
	print "NIFONLY-VALUES: @nif_not_go\n";
	print NIFONLY "$_\n" for @nif_not_go;
	
	
	# Find IRIs common to both NIF and GO lists
	my @common1 = ();
	foreach (keys %array1_hash) {
    	push(@common1, $_) if exists $array2_hash{$_};
	}
	my $common1_size = @common1;
	print "COMMON1-SIZE: $common1_size\n";
	print "COMMON1-VALUES: @common1\n";
	print COMMON1 "$_\n" for @common1;
	
	
	my @common2 = ();
	foreach (keys %array2_hash) {
		push (@common2, $_) if exists $array1_hash{$_};
	}
	my $common2_size = @common2;
	print "COMMON2-SIZE: $common2_size\n";
	print "COMMON2-VALUES: @common2\n";
	print COMMON2 "$_\n" for @common2;
	
}
	
	
close NIFLIST;
close GOLIST;
close NIFONLY;
close GOONLY;
close COMMON1;
close COMMON2;

